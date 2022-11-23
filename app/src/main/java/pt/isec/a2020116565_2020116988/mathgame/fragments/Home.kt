package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import pt.isec.a2020116565_2020116988.mathgame.MainActivity
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.SinglePlayerActivity
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentHomeBinding

/**
 * A simple [Fragment] subclass.
 * Use the [Home.newInstance] factory method to
 * create an instance of this fragment.
 */
class Home : Fragment() {

    lateinit var binding: FragmentHomeBinding;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        //var root =  inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.btnSinglePlayer.setOnClickListener {
            //findNavController().navigate(R.id.fragment_game);
            val intent = SinglePlayerActivity.getIntent(context);
            (activity as MainActivity).app.data.generateTable(1);
            startActivity(intent);
        }
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.fragment_profile);

        }
        return binding.root;
    }

    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Home().apply {

            }
    }
}