package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView

class DialogGameOver(context: Context, viewModel: SinglePlayerModelView, callback: () -> Unit) : Dialog(context)
{
    private var level: Int
    private var points: Int
    private var callback : () -> Unit
    private val viewModel : SinglePlayerModelView

    init {
        setCancelable(false)
        this.viewModel = viewModel
        this.callback = callback
        level = 0
        points = 0
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.gameover_dialog)
        level = viewModel.level.value!!
        points = viewModel.points.value!!

        findViewById<TextView>(R.id.tvGameOverLevel).text = "${context.getString(R.string.level)}: $level"
        findViewById<TextView>(R.id.tvGameOverPoints).text = "${context.getString(R.string.points)}: $points";

        findViewById<Button>(R.id.btn_gameover_menu).setOnClickListener {
            this.cancel()
            callback()
        }
    }
}