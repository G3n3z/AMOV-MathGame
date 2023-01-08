package pt.isec.a2020116565_2020116988.mathgame.ativities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.Configuration
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
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMultiplayerBinding
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.views.*


class MultiplayerActivity : AppCompatActivity() {

    companion object {

        const val MODE = "MODE"
        fun getServerModeIntent(context : Context) : Intent {
            return Intent(context, MultiplayerActivity::class.java).apply {
                putExtra(MODE, GameMode.SERVER_MODE.ordinal)
            }
        }

        fun getClientModeIntent(context : Context) : Intent {
            return Intent(context, MultiplayerActivity::class.java).apply {
                putExtra(MODE, GameMode.CLIENT_MODE.ordinal)
            }
        }
    }

    private var dlg: AlertDialog? = null
    var clientInitDialog: ClientWaitingDialog? = null


    private lateinit var binding : ActivityMultiplayerBinding;
    val app: Application by lazy { application as Application }
    private val modelView : MultiplayerModelView by viewModels{
        ViewModelFactory(app.data, 1)
    };
    lateinit var mode :GameMode


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerBinding.inflate(layoutInflater);
        setContentView(binding.root)
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
        }
        val mode :GameMode =  GameMode.gameModeByInteger(intent.getIntExtra(MODE, -1))

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

    }

    fun dialogQuit()
    {

        if (dlg?.isShowing == true)
            return;

        dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.giveup))
            .setMessage(getString(R.string.giveupMessage))
            .setPositiveButton(R.string.guOK) { d, b ->
                modelView.setLastState()
                modelView.stopGame()
                finish()
            }
            .setNegativeButton(R.string.guNOK){ d, b ->
                d.dismiss()
                modelView.cancelQuit()
            }
            .setCancelable(false)
            .create()
        dlg?.show()
    }

    override fun onBackPressed() {
        modelView.onBackPressed();
        Log.i("BACK", "On back pressed")
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
        viewModal.buttonCancel.setOnClickListener {
            dlg?.dismiss()
            modelView.stopGame()
            finish()
        }
        viewModal.apply {
            val params = ConstraintLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams = params
        }

        dlg = AlertDialog.Builder(this)
            .setTitle(R.string.server_mode)
            .setView(viewModal)
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
                    modelView.startClient(strIP, MultiplayerModelView.SERVER_PORT);
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
            .setCancelable(false)
            .setView(edtBox)
            .create()
        if(dlg?.isShowing == false)
            dlg?.show()
    }

}