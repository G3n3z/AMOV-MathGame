package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputFilter
import android.text.Spanned
import android.util.Patterns
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiplayerOptionBinding



class MultiplayerOption : Fragment() {

    private val app:Application by lazy{activity?.application as Application};
    private lateinit var binding: FragmentMultiplayerOptionBinding
    private var dlg : Dialog? = null;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMultiplayerOptionBinding.inflate(inflater, container, false);
        binding.btnServerMode.setOnClickListener { clickServerMode() }
        binding.btnClientMode.setOnClickListener { clickClientMode() }
        return binding.root;
    }

    private fun clickServerMode() {
        val wifiManager = requireContext().applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager
        val ip = wifiManager.connectionInfo.ipAddress // Deprecated in API Level 31. Suggestion NetworkCallback
        val strIPAddress = String.format("%d.%d.%d.%d",
            ip and 0xff,
            (ip shr 8) and 0xff,
            (ip shr 16) and 0xff,
            (ip shr 24) and 0xff
        )
        val ll = LinearLayout(requireActivity()).apply {
            val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            this.setPadding(50, 50, 50, 50)
            layoutParams = params
            setBackgroundColor(Color.rgb(240, 224, 208))
            orientation = LinearLayout.HORIZONTAL
            addView(ProgressBar(context).apply {
                isIndeterminate = true
                val paramsPB = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                paramsPB.gravity = Gravity.CENTER_VERTICAL
                layoutParams = paramsPB
                indeterminateTintList = ColorStateList.valueOf(Color.rgb(96, 96, 32))
            })
            addView(TextView(context).apply {
                val paramsTV = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                layoutParams = paramsTV
                text = String.format(getString(R.string.msg_ip_address),strIPAddress)
                textSize = 20f
                setTextColor(Color.rgb(96, 96, 32))
                textAlignment = View.TEXT_ALIGNMENT_CENTER
            })
        }

        dlg = AlertDialog.Builder(requireActivity())
            .setTitle(R.string.server_mode)
            .setView(ll)
            .setOnCancelListener {
               findNavController().navigate(R.id.fragment_home)
                //finish()
            }
            .create()

        startServer()

        dlg?.show()
    }

    private fun startServer() {
        //TODO
    }

    private fun clickClientMode() {
        val edtBox = EditText(requireActivity()).apply {
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
        val dlg = AlertDialog.Builder(requireContext())
            .setTitle(R.string.client_mode)
            .setMessage(R.string.ask_ip)
            .setPositiveButton(R.string.button_connect) { _: DialogInterface, _: Int ->
                val strIP = edtBox.text.toString()
                if (strIP.isEmpty() || !Patterns.IP_ADDRESS.matcher(strIP).matches()) {
                    Toast.makeText(requireContext(), R.string.error_address, Toast.LENGTH_LONG).show()
                    //finish()
                } else {
                    startClient(strIP)
                }
            }
            .setNeutralButton(R.string.btn_emulator) { _: DialogInterface, _: Int ->
                //model.startClient("10.0.2.2", SERVER_PORT-1)
                // Configure port redirect on the Server Emulator:
                // telnet localhost <5554|5556|5558|...>
                // auth <key>
                // redir add tcp:9998:9999
            }
            .setNegativeButton(R.string.button_cancel) { _: DialogInterface, _: Int ->
                //finish()
            }
            .setCancelable(false)
            .setView(edtBox)
            .create()

        dlg.show()
    }

    private fun startClient(strIP: String) {

    }

    override fun onPause() {
        super.onPause()
        dlg?.cancel()
    }

}