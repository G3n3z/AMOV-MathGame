package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AlertDialog
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivitySinglePlayerBinding

import pt.isec.a2020116565_2020116988.mathgame.fragments.GameFragment
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView

class SinglePlayerActivity : AppCompatActivity(), GameActivityInterface {
    lateinit var data: Data;
    val app: Application by lazy { application as Application }
    lateinit var binding : ActivitySinglePlayerBinding
    lateinit var fragment:GameFragment;
    lateinit var job: Job
    lateinit var maxOperation: Operation
    lateinit var secondOperation: Operation
    private var alreadyRightSecond : Boolean = false;
    private lateinit var dialog : DialogLevel;
    private var points : Int = 0
        set(value) {
            field = value
            binding.gamePont.text = "${getString(R.string.points)}: $value";
        }

    var level: Int = 0
        set(value) {
            field = value
            data.level = value
            binding.gameLevel.text = "${getString(R.string.level)}: $value";
        }
    var time: Int = 0
        set (value) {
            field = value
            data.time = value
        }
    var countRightAnswers : Int = 0

    lateinit var gamePanelView: GamePanelView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = app.data;
        //data.generateTable(data.level);
        //fragment = binding.fragmentGame.getFragment<GameFragment>();
        points = data.points;
        level = data.level;
        time = data.time;
        maxOperation = data.maxOperation
        secondOperation = data.secondOperation
        gamePanelView = GamePanelView(this,null,0,0 ,data.operations, this);
        binding.gameTable.addView(gamePanelView)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        binding.gamePont.text = "${getString(R.string.points)}: $points";
        binding.gameLevel.text = "${getString(R.string.level)}: $level";
        startTimer()

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        dialogQuit()
        Log.i("BACK", "On back pressed")
    }

    private fun startTimer(){
        Log.i("StartTimer", "On timer")
        CoroutineScope(Dispatchers.IO).async {
            job = launch { onTimer(binding.gameTime, getString(R.string.time), onTimeOver) }
        }
    }

    var onTimeOver = fun(){
        Log.i("APP", "On time over called")
    }

    companion object{

        fun getIntent(context:Context?): Intent {
            val intent = Intent(context, SinglePlayerActivity::class.java);

            return intent;
        }

    }

    override fun swipe(index: Int) {
        Log.i("Operation Max", maxOperation.toString())
        Log.i("Operation Second", secondOperation.toString())
        Log.i("SinglePlayer res: ", data.operations[index].calcOperation().toString())
        if (data.operations[index] == maxOperation){
            countRightAnswers++;
            points += 2
            alreadyRightSecond = false;
            if (countRightAnswers == 3){
                job.cancel()
                countRightAnswers = 0;
                showAnimation()
            }else {
                nextTable()
            }
        }else if (data.operations[index] == secondOperation){
            points += 1
            nextTable()
        }
    }

    private fun nextTable() {
        generateNewTable();
    }

    private fun startNewLevel() {
        startTimer()
        nextTable()
        newLevelTime()
        level += 1
    }

    private fun newLevelTime() {
        if ((time + 5) <= data.START_TIME){
            time +=5
        }else{
            time = data.START_TIME
        }
    }

    private fun generateNewTable() {
        data.generateTable(level)
        gamePanelView.mount();
        maxOperation = data.maxOperation
        secondOperation = data.secondOperation
    }

    private fun showAnimation() {
        dialog =  DialogLevel(this,this::onDialogTimeOver, 5);
        dialog.show()

    }
    fun onDialogTimeOver(){
        Log.i("OnTimeOver", "Callback called");
        startNewLevel()
    }

    private fun dialogQuit()
    {
        val dialog = AlertDialog.Builder(this)
            .setTitle(getString(R.string.giveup))
            .setMessage(getString(R.string.giveupMessage))
            .setPositiveButton(R.string.guOK) {d,b ->
                job.cancel()
                super.onBackPressed()
            }
            .setNegativeButton(R.string.guNOK)     {d,b -> d.dismiss()}
            .setCancelable(false)
            .create()
        dialog.show()
    }


    suspend fun onTimer(tv: TextView, label: String, onTimeOver: () -> Unit){

        while (true){
            delay(1000)
            time -= 1;
            CoroutineScope(Dispatchers.Main).async{
                tv.text = "${label}: ${time}";
            }
            if (time <= 0){
                onTimeOver()
                break;
            }
        }
    }


}