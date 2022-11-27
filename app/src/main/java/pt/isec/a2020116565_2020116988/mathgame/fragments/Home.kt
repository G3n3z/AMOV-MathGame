package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
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
            if((activity as MainActivity).app.data.currentUser == null){
                Snackbar.make(
                    binding.root,
                    getString(R.string.must_have_user),
                    Snackbar.LENGTH_LONG
                ).show()
                findNavController().navigate(R.id.fragment_profile);
            }else{
                val intent = SinglePlayerActivity.getIntent(context);
                (activity as MainActivity).app.data.startSinglePlayer()
                //(activity as MainActivity).app.data.generateTable(1);
                startActivity(intent);
            }
        }
        binding.btnProfile.setOnClickListener {
            findNavController().navigate(R.id.fragment_profile);
        }
        binding.btnMultiPlayer.setOnClickListener {
            findNavController().navigate(R.id.fragment_multiplayer_option)
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