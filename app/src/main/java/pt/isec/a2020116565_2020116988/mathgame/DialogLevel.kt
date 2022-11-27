package pt.isec.a2020116565_2020116988.mathgame

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView

class DialogLevel(context: Context, callback: () -> Unit, startTime : Int, viewModel: SinglePlayerModelView) : Dialog(context) {

    private var startTimer : Int;
    private var callback : () -> Unit;
    lateinit var job : Job;
    private var time :Int;
    private var paused : Boolean = false;
    private lateinit var tvTime: TextView;
    private var internalInt : Int = 0;

    private val viewModel : SinglePlayerModelView;

    init {
        this.startTimer = startTime;
        time = startTime;
        this.callback = callback;
        this.viewModel = viewModel
        setCancelable(false)
        paused = viewModel.state.value == State.OnDialogPause
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.level_dialog)
        tvTime = findViewById(R.id.tvDialogTime);
        if (!paused) {
            startJob()
        }
        tvTime.text = "Time: $time"

        findViewById<Button>(R.id.button_dialog_level).setOnClickListener {
            if (paused){
                viewModel.showAnimationResume()
                time = viewModel.currentTimeDialog
                startJob();
                (it as Button).text = "Pause"
                Log.i("Dialog", "PAUSE")
            }else{
                viewModel.showAnimationPause(time)
                job.cancel();
                Log.i("Dialog", "RESUME")
                (it as Button).text = "Resume"
            }
            paused = !paused;
        }
    }

    private fun startJob() {

        CoroutineScope(Dispatchers.IO).async{
            job = launch{
                while (true) {
                    delay(1000)
                    time -= 1;
                    tvTime.text = "Time: $time";
                    if (time <= 0){
                        this@DialogLevel.cancel()
                        viewModel.cancelDialog()
                        callback();
                        break;
                    }
                }

            }
        }
    }


}