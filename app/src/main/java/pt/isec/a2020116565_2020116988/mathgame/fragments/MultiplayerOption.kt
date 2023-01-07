package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.ativities.MultiplayerActivity
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