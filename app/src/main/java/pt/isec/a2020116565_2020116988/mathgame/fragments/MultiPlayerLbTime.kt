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
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.LBMultiPlayer
import pt.isec.a2020116565_2020116988.mathgame.data.LBPlayer
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbPointsBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbTimeBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.MpRVAdapter
import pt.isec.a2020116565_2020116988.mathgame.utils.SpRVAdapter

class MultiPlayerLbTime : Fragment() {

    private lateinit var binding: FragmentMultiPlayerLbTimeBinding
    private var listenerRegistrationGames: ListenerRegistration? = null
    private var listenerRegistrationPlayers: ListenerRegistration? = null
    private var index: Int? = null
    private var gameRefs = mutableListOf<LBMultiPlayer>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMultiPlayerLbTimeBinding.inflate(inflater, container, false)

        initiateRV()
        loadGames()

        return binding.root
    }

    private fun initiateRV() {
        binding.rvMpLbTime.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        binding.rvMpLbTime.adapter = MpRVAdapter(context)
        binding.rvMpLbTimePlayers.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        binding.rvMpLbTimePlayers.adapter = SpRVAdapter(context)

    }

    private fun loadGames(){
        val db = Firebase.firestore
        listenerRegistrationGames = db.collection(Constants.MP_DB_COLLECTION)
            .orderBy("totalTime", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }

                gameRefs = mutableListOf()
                docSS!!.documents.forEach {
                    val docRef = it.reference.id
                    val game = it.toObject(LBMultiPlayer::class.java)!!
                    game.id = docRef
                    gameRefs.add(game)
                }

                updateGames(gameRefs)

            }
    }

    private fun updateGames(gamesData: List<LBMultiPlayer>) {
        val adapter = binding.rvMpLbTime.adapter as MpRVAdapter
        adapter.addGames(gamesData)
        adapter.setOnClickListener(object :MpRVAdapter.onItemClickListener{
            override fun onItemClick(pos: Int) {
                index = pos
                loadPlayers()
            }
        })
    }

    private fun loadPlayers(){
        val db = Firebase.firestore
        listenerRegistrationPlayers = db.collection(Constants.MP_DB_COLLECTION)
            .document(gameRefs[index!!].id).collection(Constants.MP_PLAYERS_DB_COLLECTION)
            .orderBy("totalTime", Query.Direction.DESCENDING)
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

    private fun updatePlayers(playersData: List<LBPlayer>) {
        val adapter = binding.rvMpLbTimePlayers.adapter as SpRVAdapter
        adapter.addPlayers(playersData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistrationGames?.remove()
        listenerRegistrationPlayers?.remove()
    }

}