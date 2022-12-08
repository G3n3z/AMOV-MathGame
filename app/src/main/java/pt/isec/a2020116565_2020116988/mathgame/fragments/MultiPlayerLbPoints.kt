package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import android.util.Log
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
import pt.isec.a2020116565_2020116988.mathgame.data.LBMultiPlayer
import pt.isec.a2020116565_2020116988.mathgame.data.LBPlayer
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentMultiPlayerLbPointsBinding
import pt.isec.a2020116565_2020116988.mathgame.utils.MpRVAdapter
import pt.isec.a2020116565_2020116988.mathgame.utils.SpRVAdapter

class MultiPlayerLbPoints : Fragment() {

    private lateinit var binding: FragmentMultiPlayerLbPointsBinding
    private var listenerRegistration: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentMultiPlayerLbPointsBinding.inflate(inflater, container, false)

        initiateRV()
        loadGames()

        return binding.root
    }

    private fun initiateRV() {
        binding.rvMpLbPoints.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        binding.rvMpLbPoints.adapter = MpRVAdapter(context)
        binding.rvMpLbPointsPlayers.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        binding.rvMpLbPointsPlayers.adapter = SpRVAdapter(context)

    }


    private fun loadGames(){
        val db = Firebase.firestore
        listenerRegistration = db.collection(Constants.MP_DB_COLLECTION)
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { docSS, e ->
                if (e!=null) {
                    return@addSnapshotListener
                }

                val games = mutableListOf<LBMultiPlayer>()
                docSS!!.documents.forEach {
                    val docRef = it.reference.id
                    val game = it.toObject(LBMultiPlayer::class.java)!!
                    game.id = docRef
                    games.add(game)
                }

                val posi: Int
                updateGames(games)

                /*if (pos != -1){
                    gamesData[pos]
                    Log.i("TAG", gamesData[pos].toString())
                    Log.i("TAG", pos.toString())
                }*/

            }
    }

    private fun updateGames(gamesData: List<LBMultiPlayer>) {
        val adapter = binding.rvMpLbPoints.adapter as MpRVAdapter
        adapter.addGames(gamesData)
        adapter.setOnClickListener(object :MpRVAdapter.onItemClickListener{
            override fun onItemClick(pos: Int) {
                Log.i("TAG", pos.toString())
            }
        })
    }

    private fun updatePlayers(playersData: List<LBPlayer>) {
        val adapter = binding.rvMpLbPointsPlayers.adapter as SpRVAdapter
        adapter.addPlayers(playersData)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listenerRegistration?.remove()
    }

}