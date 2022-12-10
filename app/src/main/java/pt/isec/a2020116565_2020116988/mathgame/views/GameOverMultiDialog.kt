package pt.isec.a2020116565_2020116988.mathgame.views

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.User

class GameOverMultiDialog(
    context: Context,
    var users: MutableList<User>,
    var onExit: () -> Unit,
    var state: State
) : Dialog(context) {

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
        setSizePercent(90, 60)
        setCancelable(false)
        buttonExit.setOnClickListener {onExit()}

        if (state == State.WINNER){
            findViewById<TextView>(R.id.tv_multi_player_game_over_modal).text = context.getString(R.string.winner)
        }

    }

    fun update(users : MutableList<User>){
        adapter.submitNewData(users)
    }

    private fun setSizePercent(percentage: Int, percentageHeight : Int) {
        val percent = percentage.toFloat() / 100
        val percentH = percentageHeight.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        val percentHeight = rect.height() * percentH
        window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
    }



}