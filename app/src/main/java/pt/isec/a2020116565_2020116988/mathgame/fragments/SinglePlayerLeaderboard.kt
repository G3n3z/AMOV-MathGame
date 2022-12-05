package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentSinglePlayerLeaderboardBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.SpRVAdapter


class SinglePlayerLeaderboard : Fragment() {

    private lateinit var binding: FragmentSinglePlayerLeaderboardBinding
    data class Info(val str1:String,val str2 : String, val str3:String)
    val info = arrayListOf<Info>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSinglePlayerLeaderboardBinding.inflate(inflater, container, false)

        val item = Info("pos", "${0}", "${0}")
        info.add(item)

        binding.rvSpLb.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)

        binding.rvSpLb.adapter = SpRVAdapter(info)

        return binding.root
    }

    companion object {

    }
}