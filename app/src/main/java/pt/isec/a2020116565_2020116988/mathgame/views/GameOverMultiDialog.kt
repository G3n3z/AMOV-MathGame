package pt.isec.a2020116565_2020116988.mathgame.views

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.utils.setSizePercent

class GameOverMultiDialog(context: Context, var users: MutableList<User>, var onExit: () -> Unit) : Dialog(context) {

    lateinit var recycler : RecyclerView;
    lateinit var adapter :ScoresRecycleViewAdapter
    lateinit var buttonExit : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.multiplayer_game_over)

        buttonExit = findViewById<Button>(R.id.btn_multi_player_game_over_modal)
        recycler = findViewById<RecyclerView>(R.id.view_multi_player_game_over_modal)
        recycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
        adapter = ScoresRecycleViewAdapter(users)
        recycler.adapter = adapter

        val orientation = context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSizePercent(90, 90, window)
        }else{
            setSizePercent(90, 40, window)
        }
        setCancelable(false)
        buttonExit.setOnClickListener {onExit()}
    }

    fun update(users : MutableList<User>){
        adapter.submitNewData(users)
    }

}