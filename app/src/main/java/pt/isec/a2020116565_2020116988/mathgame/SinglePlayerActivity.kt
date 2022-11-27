package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.data.SinglePlayerModelView
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivitySinglePlayerBinding

import pt.isec.a2020116565_2020116988.mathgame.fragments.GameFragment
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView

class ViewModelFactory(private val data: Data): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = SinglePlayerModelView(data) as T
}


class SinglePlayerActivity : AppCompatActivity(), GameActivityInterface {

    lateinit var data: Data;
    val app: Application by lazy { application as Application }
    lateinit var binding : ActivitySinglePlayerBinding
    lateinit var fragment:GameFragment;
    var job: Job? = null
    var dlg : AlertDialog? = null
    lateinit var maxOperation: Operation
    lateinit var secondOperation: Operation
    private var alreadyRightSecond : Boolean = false;
    private var dialog : DialogLevel? = null;
    private var gameOverDialog : DialogGameOver? = null
    private var points : Int = 0
        set(value) {
            field = value
            binding.gamePont.text = "${getString(R.string.points)}: $value";
        }

    var level: Int = 0
        set(value) {
            field = value
            //data.level = value
            binding.gameLevel.text = "${getString(R.string.level)}: $value";
        }
    var time: Int = 0
        set (value) {
            field = value
            //data.time = value
            binding.gameTime.text = getString(R.string.time) + ": ${value}";
        }


    lateinit var gamePanelView: GamePanelView

    private val modelView : SinglePlayerModelView by viewModels{
        ViewModelFactory(app.data)
    };


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = app.data;

        points = data.points;
        level = data.level;
        time = data.time;
        maxOperation = data.maxOperation
        secondOperation = data.secondOperation
        gamePanelView = GamePanelView(this,null,0,0, data.operations, this);
        binding.gameTable.addView(gamePanelView)
        registerCallbacksOnState();
        registerCallbacksOnLabels()

        Log.i("OnCreate", modelView.state.value.toString())

    }

    private fun registerCallbacksOnLabels() {
        modelView.time.observe(this){
            time = it
        }
        modelView.level.observe(this){
            level = it;
        }
        modelView.points.observe(this){
            points = it;
        }
        modelView.operation.observe(this){
            gamePanelView.operations = it
            gamePanelView.mount()
            Log.i("Operation Max", data.maxOperation.toString())
            Log.i("Operation Second", data.secondOperation.toString())
        }
    }

    private fun registerCallbacksOnState() {
        modelView.state.observe(this){
            onStateChange(it);
        }
    }

    private fun onStateChange(state :State) {
        when(state){
            State.OnGame -> {
                startTimer()
                Log.i("onStateChange", "OnGame");
            }
            State.OnDialogBack -> {
                startTimer()
                dialogQuit()
            }
            State.OnDialogResume -> {
                Log.i("onStateChange", "OnDialogResume");
                showAnimation()
                stopJob()
            }
            State.OnDialogPause -> {
                showAnimation()
                Log.i("onStateChange", "OnDialogPause");
            }
            State.OnGameOver ->{
                showGameOverDialog()
            }
        }
    }

    private fun stopJob() {
        if (job?.isActive == true){
            job?.cancel()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        Log.i("OnStart", modelView.state.value.toString())
        modelView.refreshState()
        binding.gamePont.text = "${getString(R.string.points)}: $points";
        binding.gameLevel.text = "${getString(R.string.level)}: $level";

    }

    override fun onPause() {
        super.onPause()
        dialog?.cancel()
        job?.cancel()
        dialog = null
        dlg?.cancel()
    }
    override fun onBackPressed() {
        if(modelView.state.value == State.OnGame)
            modelView.onBackPressed();
        Log.i("BACK", "On back pressed")
    }

    private fun startTimer(){
        if(job == null || job?.isActive == false) {
            Log.i("StartTimer", "On timer")
            CoroutineScope(Dispatchers.IO).async {
                job = launch { onTimer(binding.gameTime, getString(R.string.time), onTimeOver) }
            }
        }
    }

    var onTimeOver = fun(){
        dlg?.cancel()
        modelView.onGameOver()
        Log.i("APP", "On time over called")
    }


    override fun swipe(index: Int) {
        Log.i("Operation Max", data.maxOperation.toString())
        Log.i("Operation Second", data.secondOperation.toString())
        Log.i("SinglePlayer res: ", data.operations[index].calcOperation().toString())
        modelView.swipe(index)
    }


    private fun showAnimation() {
        if (dialog == null) {
            dialog = DialogLevel(this, this::onDialogTimeOver, modelView.currentTimeDialog, modelView);
            dialog?.show()
        }
    }

    fun onDialogTimeOver(){
        Log.i("OnTimeOver", "Callback called");
        dialog = null
        dlg?.cancel()
        modelView.startNewLevel()
    }

    private fun dialogQuit()
    {
        if (dlg?.isShowing == true)
            return;

         dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.giveup))
            .setMessage(getString(R.string.giveupMessage))
            .setPositiveButton(R.string.guOK) {d,b ->
                job?.cancel()
                super.onBackPressed()
            }
            .setNegativeButton(R.string.guNOK){d,b ->
                d.dismiss()
                modelView.cancelQuit()
            }
            .setCancelable(false)
            .create()
        dlg?.show()
    }

    private fun showGameOverDialog()
    {
        if(gameOverDialog?.isShowing == true)
            return
        gameOverDialog = DialogGameOver(this, modelView, onGameOverDialogClose)
        gameOverDialog?.show()

    }

    private var onGameOverDialogClose = fun(opt: Int){
        when(opt){
            1 -> {
                Log.i("CALLBACK", "Nav to TOP 5")
                finish()
            }//TODO navigate to top 5
            2 -> {
                finish()
            }
        }
    }

    suspend fun onTimer(tv: TextView, label: String, onTimeOver: () -> Unit){

        while (true){
            delay(1000)
            CoroutineScope(Dispatchers.Main).async{
                modelView.decTime()
            }
            if (time <= 0){
                onTimeOver()
                break;
            }
        }
    }

    companion object{

        fun getIntent(context:Context?): Intent {
            val intent = Intent(context, SinglePlayerActivity::class.java);

            return intent;
        }

    }

}
