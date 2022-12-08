package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.LBPlayer
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentSinglePlayerLeaderboardBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.SpRVAdapter


class SinglePlayerLeaderboard : Fragment() {

    private lateinit var binding: FragmentSinglePlayerLeaderboardBinding
    private var listenerRegistration: ListenerRegistration? = null

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
        val db = Firebase.firestore
        listenerRegistration = db.collection(Constants.SP_DB_COLLECTION)
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }

                val playersData = docSS!!.map {
                    it.toObject(LBPlayer::class.java)
                }

                updatePlayers(playersData)
            }
    }

    /**
     * Metodo para enviar os dados da lista de jogadores para a vista
     */
    private fun updatePlayers(playersData: List<LBPlayer>) {
        val adapter = binding.rvSpLb.adapter as SpRVAdapter
        adapter.addPlayers(playersData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }
}