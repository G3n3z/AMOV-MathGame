package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMultiplayerBinding
import pt.isec.a2020116565_2020116988.mathgame.dialog.DialogLevelMultiplayer
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.views.*


class MultiplayerActivity : AppCompatActivity(), GameActivityInterface {

    companion object {

        private const val MODE = "MODE"
        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context,MultiplayerActivity::class.java).apply {
                putExtra(MODE, GameMode.SERVER_MODE.ordinal)
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context,MultiplayerActivity::class.java).apply {
                putExtra(MODE, GameMode.CLIENT_MODE.ordinal)
            }
        }
    }
    private var job :Job? = null;
    private var dlg: AlertDialog? = null
    private var clientInitDialog: ClientWaitingDialog? = null
    private var dialog : DialogLevelMultiplayer? = null
    private var adapter : ScoresRecycleViewAdapter? = null;
    private var dialogGameOver : GameOverMultiDialog? = null;
    private var points : Int = 0
        set(value) {
            field = value
            binding.gamePontMultiplayer.text = "${getString(R.string.points)}: $value";
        }

    var level: Int = 0
        set(value) {
            field = value
            //data.level = value
            binding.gameLevel.text = "${getString(R.string.level)}: $value";
        }
    var time: Int = 0
        set (value) {
            field = value
            //data.time = value
            binding.gameTimeMultiplayer.text = getString(R.string.time) + ": ${value}";
        }

    private lateinit var binding : ActivityMultiplayerBinding;
    val app: Application by lazy { application as Application }
    private val modelView : MultiplayerModelView by viewModels{
        ViewModelFactory(app.data, 1)
    };
    lateinit var gamePanelView : GamePanelView;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater);
        setContentView(binding.root)
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
        }
        val mode :GameMode =  GameMode.gameModeByInteger(intent.getIntExtra(MODE, -1))
        Log.i("ATG", intent.getIntExtra(MODE, -1).toString())
        Log.i("ATG", mode.toString())

        if (mode == GameMode.SERVER_MODE && modelView.connectionState.value == ConnectionState.CONNECTING){
            Log.i("Server", "SERVER_MODE")
            modelView.setMode(GameMode.SERVER_MODE)
            serverMode()

        }
        else if(mode == GameMode.CLIENT_MODE && modelView.connectionState.value == ConnectionState.CONNECTING){
            Log.i("Server", "CLIENT_MODE")
            modelView.setMode(GameMode.CLIENT_MODE)
            clientMode()

        }

         modelView.connectionState.observe(this) { connectionStateHandlers(it, mode) }

        gamePanelView = GamePanelView(this,null,0,0, app.data.operations, this);
        binding.gameTableMultiplayer.addView(gamePanelView)
        registerCallbacksOnState();
        registerCallbacksOnLabels();
    }

//    private fun connectionStateHandlersServer(it: ConnectionState?) {
//        if(it == ConnectionState.CONNECTION_ESTABLISHED){
//            binding.flScoresFragment.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
//            adapter = ScoresRecycleViewAdapter(modelView.users.value!!)
//            binding.flScoresFragment.adapter = adapter
//        }
//        else if(it == ConnectionState.CONNECTION_LOST && modelView._state.value!! == State.OnGame){
//            val intent = SinglePlayerActivity.getIntentFromMultiplayer(this, modelView.state.value?.ordinal!!)
//            app.data.generateMaxOperations();
//            finish()
//            startActivity(intent);
//        }
//        else if(it == ConnectionState.CONNECTION_LOST && modelView._state.value!! == State.OnGameOver){
//            finish()
//        }
//    }

    private fun connectionStateHandlers(it: ConnectionState, mode : GameMode) {
        if (it == ConnectionState.WAITING_OTHERS && clientInitDialog == null){
            if(mode == GameMode.CLIENT_MODE) {
                clientInitDialog = ClientWaitingDialog(cancelWait)
                clientInitDialog?.show(supportFragmentManager, "waitingFrag")
            }
        }else if(it == ConnectionState.CONNECTION_ESTABLISHED){
            if(mode == GameMode.CLIENT_MODE){
                clientInitDialog?.dismiss()
                clientInitDialog = null
            }
            binding.flScoresFragment.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
            adapter = ScoresRecycleViewAdapter(modelView.users.value!!)
            binding.flScoresFragment.adapter = adapter;
        }else if(it == ConnectionState.CONNECTION_LOST && modelView._state.value!! == State.OnGameOver){
            finish()
        }else if(it == ConnectionState.CONNECTION_LOST && modelView._state.value!! == State.OnGame ||
            modelView._state.value!! == State.OnDialogPause){
            Snackbar.make(binding.root, getString(R.string.connection_lost), Snackbar.LENGTH_LONG).show()
            dialog?.cancel()
            val intent = SinglePlayerActivity.getIntentFromMultiplayer(this, modelView.state.value?.ordinal!!)
            app.data.generateMaxOperations();
            finish()
            startActivity(intent);
        }else if (it == ConnectionState.EXIT){
            modelView.closeSockets()
            finish()
        }
    }

    private var cancelWait = fun(){
        modelView.stopGame()
        finish()
    }



    private fun registerCallbacksOnLabels() {
        modelView.time.observe(this){
            time = it
        }
        modelView.level.observe(this){
            level = it;
        }
        modelView.points.observe(this){
            points = it;
        }
        modelView.operation.observe(this){
            gamePanelView.operations = it
            gamePanelView.mount()
        }
        modelView.users.observe(this){
            Log.i("registerCall", "reciclerView update")
            adapter?.submitNewData(it)
            dialogGameOver?.update(it);
        }
    }

    override fun onPause() {
        super.onPause()
        dlg?.cancel()
        dialogGameOver?.dismiss()
    }

    override fun swipe(index: Int) {
        modelView.swipe(index)
    }

    private fun registerCallbacksOnState() {
        modelView.state.observe(this){
            onStateChange(it);
        }
    }

    private fun onStateChange(state :State) {
        when(state){
            State.OnGame -> {
                dialog?.dismiss()
                dialog = null
            }
            State.OnDialogBack -> {
                if (clientInitDialog?.isVisible == true){
                    clientInitDialog!!.dismiss()
                    clientInitDialog = null
                    finish()
                }
                dialogQuit()
            }
            State.OnDialogResume -> {
                Log.i("onStateChange", "OnDialogResume");
                showAnimation()

            }
            State.OnDialogPause -> {
                showAnimation()
                Log.i("onStateChange", "OnDialogPause");

            }
            State.OnGameOver ->{
                if (dialogGameOver == null || dialogGameOver?.isShowing == false){
                    dialogGameOver = GameOverMultiDialog(this, modelView.users.value!!, this::onExit)
                    dialogGameOver?.show()
                }
            }
        }
    }

    private fun stopJob() {
        if (job?.isActive == true){
            job?.cancel()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        Log.i("OnStart", modelView.state.value.toString())
        modelView.refreshState()
        binding.gamePontMultiplayer.text = "${getString(R.string.points)}: $points";
        binding.gameLevel.text = "${getString(R.string.level)}: $level";

    }

    override fun onBackPressed() {
        modelView.onBackPressed();
        Log.i("BACK", "On back pressed")
    }


    private fun showAnimation() {
        if (dialog == null) {
            dialog = DialogLevelMultiplayer(this)
            dialog?.show()
        }

    }


    fun onExit(){
        modelView.stopGame()
        finish()
    }

    private fun dialogQuit()
    {
        if (dlg?.isShowing == true)
            return;

        dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.giveup))
            .setMessage(getString(R.string.giveupMessage))
            .setPositiveButton(R.string.guOK) {d,b ->
                job?.cancel()
                modelView.stopGame()
                super.onBackPressed()
                finish()
            }
            .setNegativeButton(R.string.guNOK){d,b ->
                d.dismiss()
                modelView.cancelQuit()
            }
            .setCancelable(false)
            .create()
        dlg?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        modelView.users.removeObservers(this)
        modelView.time.removeObservers(this)
        modelView.points.removeObservers(this)
        modelView.level.removeObservers(this)
        modelView.nConnections.removeObservers(this)
        modelView.state.removeObservers(this)

    }
    private fun serverMode() {
        val wifiManager = applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )
        var viewModal = ServerModalInitial(this, null, 0, 0)

        val ll = LinearLayout(this).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            viewModal.tvIp.text = String.format(getString(R.string.msg_ip_address),strIPAddress)
            viewModal.tvClients.text = getString(R.string.num_of_clients) + ": " + modelView.nConnections.value.toString()
            viewModal.button.isEnabled = false;

            modelView.nConnections.observe(this@MultiplayerActivity){
                if (dlg?.isShowing == true) {
                    viewModal.button.isEnabled = modelView.nConnections.value!! > 0
                    Log.i("Chegou cliente", modelView.nConnections.value.toString())
                    viewModal.tvClients.text = getString(R.string.num_of_clients) + ": " + modelView.nConnections.value.toString()
                }
            }
            viewModal.button.setOnClickListener{
                modelView.startGameInServer();
                dlg?.dismiss()
            }
            addView(viewModal)
        }

        dlg = AlertDialog.Builder(this)
            .setTitle(R.string.server_mode)
            .setView(ll)
            .setOnCancelListener {
                //finish()
            }
            .setCancelable(false)
            .create()

        modelView.startServer()

        dlg?.show()
    }

    private fun clientMode() {
        val edtBox = EditText(this).apply {
            maxLines = 1
            filters = arrayOf(object : InputFilter {
                override fun filter(
                    source: CharSequence?,
                    start: Int,
                    end: Int,
                    dest: Spanned?,
                    dstart: Int,
                    dend: Int
                ): CharSequence? {
                    source?.run {
                        var ret = ""
                        forEach {
                            if (it.isDigit() || it == '.')
                                ret += it
                        }
                        return ret
                    }
                    return null
                }

            })
        }
        dlg = AlertDialog.Builder(this)
            .setTitle(R.string.client_mode)
            .setMessage(R.string.ask_ip)
            .setPositiveButton(R.string.button_connect) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(
                        this@MultiplayerActivity,
                        R.string.error_address,
                        Toast.LENGTH_LONG
                    ).show()
                    //finish()
                } else {
                    Log.i("dialog ip",strIP )
                    modelView.startClient(strIP, MultiplayerModelView.SERVER_PORT-1);
                }
            }
            .setNeutralButton(R.string.btn_emulator) { _: DialogInterface, _: Int ->
                modelView.startClient("10.0.2.2", MultiplayerModelView.SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                finish()
            }
            .setCancelable(true)
            .setView(edtBox)
            .create()
        if(dlg?.isShowing == false)
            dlg?.show()
    }

}