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
import pt.isec.a2020116565_2020116988.mathgame.MultiplayerActivity
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
        binding.btnServerMode.setOnClickListener {
            app.data.clear()
            val intent = MultiplayerActivity.getServerModeIntent(requireContext())
            startActivity(intent)
        }
        binding.btnClientMode.setOnClickListener {
            app.data.clear()
            val intent = MultiplayerActivity.getClientModeIntent(requireContext())
            startActivity(intent)
        }
        return binding.root;
    }



}