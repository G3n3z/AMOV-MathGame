package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentTop5OptionBinding


class Top5Option : Fragment() {

    private lateinit var binding: FragmentTop5OptionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentTop5OptionBinding.inflate(inflater, container, false)

        binding.btnSpTop5.setOnClickListener {
            findNavController().navigate(R.id.fragment_sp_leaderboard)
        }
        binding.btnMpTop5Pts.setOnClickListener {
            findNavController().navigate(R.id.fragment_mp_leaderboard_points)
        }
        binding.btnMpTop5Time.setOnClickListener {
            findNavController().navigate(R.id.fragment_mp_leaderboard_time)
        }

        return binding.root
    }
}