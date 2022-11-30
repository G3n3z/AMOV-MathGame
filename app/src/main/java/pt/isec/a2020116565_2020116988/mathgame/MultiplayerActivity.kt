package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.util.Patterns
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMultiplayerBinding
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.views.ClientWaitingDialog
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView
import pt.isec.a2020116565_2020116988.mathgame.views.ServerModalInitial


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
    private lateinit var clientInitDialog: ClientWaitingDialog
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
        var mode :GameMode =  GameMode.gameModeByInteger(intent.getIntExtra(MODE, -1))
        Log.i("ATG", intent.getIntExtra(MODE, -1).toString())
        Log.i("ATG", mode.toString())
        modelView.setMode(mode);

        if (mode == GameMode.SERVER_MODE && modelView.connectionState.value == ConnectionState.CONNECTING){
            Log.i("Server", "SERVER_MODE")
            serverMode()

        }
        else if(mode == GameMode.CLIENT_MODE && modelView.connectionState.value == ConnectionState.CONNECTING){
            Log.i("Server", "CLIENT_MODE")
            clientMode()
        }
        if (mode == GameMode.CLIENT_MODE) {
            modelView.connectionState.observe(this) { connectionStateHandlers(it) }
        }
        gamePanelView = GamePanelView(this,null,0,0, app.data.operations, this);
        binding.gameTableMultiplayer.addView(gamePanelView)
        registerCallbacksOnState();
        registerCallbacksOnLabels();
    }

    private fun connectionStateHandlers(it: ConnectionState) {
        if (it == ConnectionState.WAITING_OTHERS){
            clientInitDialog = ClientWaitingDialog(this)
            clientInitDialog.show()

        }else if(it == ConnectionState.CONNECTION_ESTABLISHED){
            Log.i("connectionStateHandlers", clientInitDialog.isShowing.toString())
            if(clientInitDialog.isShowing)
                clientInitDialog.cancel()
        }

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
                if (dlg?.isShowing == true && modelView.nConnections.value!! > 0){
                    viewModal.button.isEnabled = true
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
                    modelView.startClient(strIP);
                }
            }
            .setNeutralButton(R.string.btn_emulator) { _: DialogInterface, _: Int ->
                modelView.startClient("10.0.2.2", modelView.SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                finish()
            }
            .setCancelable(false)
            .setView(edtBox)
            .create()
        if(dlg?.isShowing == false)
            dlg?.show()
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
    }

    override fun onPause() {
        super.onPause()
        dlg?.cancel()
    }

    override fun swipe(index: Int) {
        //TODO
    }

    private fun registerCallbacksOnState() {
        modelView.state.observe(this){
            onStateChange(it);
        }
    }

    private fun onStateChange(state :State) {
        when(state){
            State.OnGame -> {
                if(modelView.connectionState.value == ConnectionState.CONNECTION_ESTABLISHED){
                    startTimer()
                    Log.i("onStateChange", "OnGame");
                }
            }
            State.OnDialogBack -> {
                startTimer()
                dialogQuit()
            }
            State.OnDialogResume -> {
                Log.i("onStateChange", "OnDialogResume");
                showAnimation()
                stopJob()
            }
            State.OnDialogPause -> {
                showAnimation()
                Log.i("onStateChange", "OnDialogPause");
            }
            State.OnGameOver ->{}
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
        //Todo fechar server socket se carregar no sim
        modelView.onBackPressed();
        Log.i("BACK", "On back pressed")
    }

    private fun startTimer(){
        if(job == null || job?.isActive == false && modelView.connectionState.value == ConnectionState.CONNECTION_ESTABLISHED) {
            Log.i("StartTimer", "On timer")
            CoroutineScope(Dispatchers.IO).async {
                job = launch { onTimer(binding.gameTimeMultiplayer, getString(R.string.time), onTimeOver) }
            }
        }
    }

    var onTimeOver = fun(){
        Log.i("APP", "On time over called")
    }


    private fun showAnimation() {
//        if (dialog == null) {
//            dialog = DialogLevel(this, this::onDialogTimeOver, modelView.currentTimeDialog, modelView);
//            dialog?.show()
//        }
    }

    fun onDialogTimeOver(){
        Log.i("OnTimeOver", "Callback called");
        //dialog = null
        dlg?.cancel()
        modelView.startNewLevel()
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
            }
            .setNegativeButton(R.string.guNOK){d,b ->
                d.dismiss()
                modelView.cancelQuit()
            }
            .setCancelable(false)
            .create()
        dlg?.show()
    }


    suspend fun onTimer(tv: TextView, label: String, onTimeOver: () -> Unit){

        while (true){
            delay(1000)
            CoroutineScope(Dispatchers.Main).async{
                modelView.decTime()
            }
            if (time <= 0){
                onTimeOver()
                break;
            }
        }
    }


}