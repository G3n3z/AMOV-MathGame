package pt.isec.a2020116565_2020116988.mathgame

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
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMultiplayerBinding
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.views.ServerModalInitial


class MultiplayerActivity : AppCompatActivity() {

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

    private var dlg: AlertDialog? = null
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
                if (dlg?.isShowing == true){
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
                finish()
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


    override fun onPause() {
        super.onPause()
        dlg?.cancel()
    }

}