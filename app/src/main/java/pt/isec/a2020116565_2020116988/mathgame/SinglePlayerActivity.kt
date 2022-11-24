package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivitySinglePlayerBinding

import pt.isec.a2020116565_2020116988.mathgame.fragments.GameFragment
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.utils.onTimer
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView
import kotlin.math.max

class SinglePlayerActivity : AppCompatActivity(), GameActivityInterface {
    lateinit var data: Data;
    val app: Application by lazy { application as Application }
    lateinit var binding : ActivitySinglePlayerBinding
    lateinit var fragment:GameFragment;
    lateinit var job: Job
    lateinit var maxOperation: Operation
    lateinit var secondOperation: Operation
    var points : Int = 0
        set(value) {
            field = value
            binding.gamePont.text = "${getString(R.string.points)}: $value";
        }
    var level: Int = 0;
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
        gamePanelView = GamePanelView(this, data.operations, this);
        binding.gameTable.addView(gamePanelView)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()
        binding.gamePont.text = "${getString(R.string.points)}: $points";
        binding.gameLevel.text = "${getString(R.string.level)}: $level";
        //val panel = GamePanelView(this, data.operations, this)
//        binding.gameTable.owner = this
//        binding.gameTable.operations = data.operations
//        binding.gameTable.mount()
        startTimer()

    }

    fun startTimer(){
        CoroutineScope(Dispatchers.IO).async {
            job = launch { onTimer(binding.gameTime, getString(R.string.time), time) }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        if (fragment.gestureDetector.onTouchEvent(event!!)){
//            return true;
//        }
        return super.onTouchEvent(event)
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
            generateNewTable();
        }else if (data.operations[index] == secondOperation){
            points += 1
        }
    }

    private fun generateNewTable() {
        //binding.fragmentGame.re = GameFragment()
    }

}