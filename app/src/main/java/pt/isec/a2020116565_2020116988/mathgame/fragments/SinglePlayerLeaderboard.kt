package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import pt.isec.a2020116565_2020116988.mathgame.data.LBPlayer
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentSinglePlayerLeaderboardBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.SpRVAdapter


class SinglePlayerLeaderboard : Fragment() {

    private lateinit var binding: FragmentSinglePlayerLeaderboardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentSinglePlayerLeaderboardBinding.inflate(inflater, container, false)

        initiateRV()
        loadPlayers()

        return binding.root
    }

    /**
     * Metodo para inicializar a recycle view
     */
    private fun initiateRV(){
        binding.rvSpLb.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        binding.rvSpLb.adapter = SpRVAdapter(context)
    }

    /**
     * Metodo para carregar os dados da Firestore para uma lista de jogadores
     */
    private fun loadPlayers(){
        //TODO buscar dados a Firestore e mapear alterar os nullables
        //TODO inicializar o listener
        val players: MutableList<LBPlayer> = mutableListOf()
        val item = LBPlayer()
        players.add(item)
        players.add(item)
        players.add(item)
        players.add(item)
        players.add(item)
        players.add(item)
        players.add(item)

        updatePlayers(players.take(5))
    }

    /**
     * Metodo para enviar os dados da lista de jogadores para a vista
     */
    private fun updatePlayers(players: List<LBPlayer>?) {
        val adapter = binding.rvSpLb.adapter as SpRVAdapter
        adapter.addPlayers(players!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        //TODO cancelar o listener
    }
}