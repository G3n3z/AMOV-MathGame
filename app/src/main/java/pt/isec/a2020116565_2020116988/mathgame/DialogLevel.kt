package pt.isec.a2020116565_2020116988.mathgame

import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView
import pt.isec.a2020116565_2020116988.mathgame.utils.setSizePercent

class DialogLevel(context: Context, callback: () -> Unit, startTime : Int, viewModel: SinglePlayerModelView) : Dialog(context) {

    private var startTimer : Int;
    private var callback : () -> Unit;
    var job : Job? = null;
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

        val orientation = context.resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setSizePercent(60, 90, window)
        }else{
            setSizePercent(90, 60, window)
        }
        tvTime = findViewById(R.id.tvDialogTime);
        val button = findViewById<Button>(R.id.button_dialog_level);
        val pauseText = context.getString(R.string.pause)
        val resumeText = context.getString(R.string.resume)
        val timeText = context.getString(R.string.time)
        if (paused) {
            button.text = resumeText
        }else{
            startJob()
            button.text = pauseText
        }
        tvTime.text = String.format("%s %d", timeText, time)

        button.setOnClickListener {
            if (paused){
                viewModel.showAnimationResume()
                time = viewModel.currentTimeDialog
                startJob();
                (it as Button).text = pauseText
                Log.i("Dialog", "PAUSE")
            }else{
                viewModel.showAnimationPause(time)
                job?.cancel();
                Log.i("Dialog", "RESUME")
                (it as Button).text = resumeText
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
                    viewModel.currentTimeDialog = time;
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

    override fun onStop() {
        super.onStop()
        job?.cancel()
    }
}