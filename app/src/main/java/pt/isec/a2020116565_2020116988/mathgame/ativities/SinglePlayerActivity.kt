package pt.isec.a2020116565_2020116988.mathgame.ativities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.*
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivitySinglePlayerBinding
import pt.isec.a2020116565_2020116988.mathgame.dialog.DialogGameOver
import pt.isec.a2020116565_2020116988.mathgame.dialog.DialogLevel
import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import pt.isec.a2020116565_2020116988.mathgame.utils.vibratePhone
import pt.isec.a2020116565_2020116988.mathgame.views.GamePanelView
import kotlin.concurrent.thread

class ViewModelFactory(private val data: Data, private val type:Int): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (type == 0){
            return SinglePlayerModelView(data) as T
        }else{
            return MultiplayerModelView(data) as T
        }
    }
}


class SinglePlayerActivity : AppCompatActivity(), GameActivityInterface {

    lateinit var data: Data
    val app: Application by lazy { application as Application }
    lateinit var binding : ActivitySinglePlayerBinding
    var job: Job? = null
    var jobResult: Job? = null
    var dlg : AlertDialog? = null
    lateinit var maxOperation: Operation
    lateinit var secondOperation: Operation
    private var dialog : DialogLevel? = null
    private var gameOverDialog : DialogGameOver? = null
    private var points : Int = 0
        set(value) {
            field = value
            binding.gamePont.text = String.format("%s: %d", getString(R.string.points), value)
        }

    var level: Int = 0
        set(value) {
            field = value
            binding.gameLevel.text = String.format("%s: %d", getString(R.string.level), value)
        }
    var time: Int = 0
        set (value) {
            field = value
            binding.gameTime.text = String.format("%s: %d", getString(R.string.time), value)
        }


    lateinit var gamePanelView: GamePanelView
    private var flag: Int = -1

    private val modelView : SinglePlayerModelView by viewModels{
        ViewModelFactory(app.data, 0)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = app.data

        points = data.points
        level = data.level
        time = data.time
        maxOperation = data.maxOperation
        secondOperation = data.secondOperation

        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
        }


        gamePanelView = GamePanelView(this,null,0,0, data.operations, this)
        binding.gameTable.addView(gamePanelView)
        registerCallbacksOnState()
        registerCallbacksOnLabels()
        getStateByInt()
        Log.i("OnCreate", modelView.state.value.toString())

    }

    private fun getStateByInt() {
        val stateExtra = intent.getIntExtra(STATE, -1)
        val flagExtra = intent.getIntExtra(FLAG, -1)
        flag = flagExtra
        if (stateExtra == -1)
            return
        Snackbar.make(binding.root, getString(R.string.connection_lost), Snackbar.LENGTH_LONG).show()
        intent.putExtra(STATE,-1)
    }

    private fun registerCallbacksOnLabels() {
        modelView.time.observe(this){
            time = it
        }
        modelView.level.observe(this){
            level = it
        }
        modelView.points.observe(this){
            points = it
        }
        modelView.moveResult.observe(this){
            jobResult?.cancel()
            when(it) {
                MoveResult.NOTHING -> {binding.moveResponse.text = ""}
                MoveResult.WRONG_OPERATION -> {
                    binding.moveResponse.text = getString(R.string.wrong_response)
                    binding.moveResponse.setTextColor(Color.RED)
                    vibratePhone(this)
                }
                MoveResult.MAX_OPERATION -> {
                    binding.moveResponse.text = getString(R.string.right_answers)
                    binding.moveResponse.setTextColor(Color.GREEN)
                }
                MoveResult.SECOND_OPERATION ->{
                    binding.moveResponse.text = getString(R.string.second_answers)
                    binding.moveResponse.setTextColor(Color.BLUE)
                }
            }
            if(it != MoveResult.NOTHING){
                jobResult = CoroutineScope(Dispatchers.IO).launch{ clean() }
            }

        }

        modelView.operation.observe(this){
            gamePanelView.operations = it
            gamePanelView.mount()
            Log.i("Operation Max", data.maxOperation.toString())
            Log.i("Operation Second", data.secondOperation.toString())
        }
    }



    private suspend fun clean(){
        delay(1000)
        binding.moveResponse.post{binding.moveResponse.text = ""}
    }

    private fun registerCallbacksOnState() {
        modelView.state.observe(this){
            onStateChange(it)
        }
    }

    private fun onStateChange(state : State) {
        when(state){
            State.OnGame -> {
                modelView.startTimer()
                Log.i("onStateChange", "OnGame")
            }
            State.OnDialogBack -> {
                //startTimer()
                dialogQuit()
            }
            State.OnDialogResume -> {
                Log.i("onStateChange", "OnDialogResume")
                modelView.stopTimer()
                showAnimation()
                stopJob()
            }
            State.OnDialogPause -> {
                showAnimation()
                Log.i("onStateChange", "OnDialogPause")
            }
            State.OnGameOver, State.WINNER ->{
                dlg?.cancel()
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
        binding.gamePont.text = "${getString(R.string.points)}: $points"
        binding.gameLevel.text = "${getString(R.string.level)}: $level"

    }

    override fun onPause() {
        super.onPause()
        dialog?.cancel()
        job?.cancel()
        dialog = null
        dlg?.cancel()
        gameOverDialog?.cancel()
        gameOverDialog = null
        jobResult?.cancel()
        binding.moveResponse?.text = ""
    }
    override fun onBackPressed() {
        if(modelView.state.value == State.OnGame)
            modelView.onBackPressed()
        Log.i("BACK", "On back pressed")
    }

    private fun startTimer(){
//        if(job == null || job?.isActive == false) {
//            Log.i("StartTimer", "On timer")
//            job = CoroutineScope(Dispatchers.IO).launch {
//                 onTimer(binding.gameTime, getString(R.string.time), onTimeOver)
//            }
//        }
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
            dialog = DialogLevel(this, this::onDialogTimeOver, modelView.currentTimeDialog, modelView)
            dialog?.show()
        }
    }

    private fun onDialogTimeOver(){
        Log.i("OnTimeOver", "Callback called")
        dialog = null
        dlg?.cancel()
        modelView.startNewLevel()
    }

    private fun dialogQuit()
    {
        if (dlg?.isShowing == true)
            return

        dlg = AlertDialog.Builder(this)
            .setTitle(getString(R.string.giveup))
            .setMessage(getString(R.string.giveupMessage))
            .setPositiveButton(R.string.guOK) { d, b ->
                job?.cancel()
                super.onBackPressed()
            }
            .setNegativeButton(R.string.guNOK){ d, b ->
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
        gameOverDialog!!.show()
    }

    private var onGameOverDialogClose = fun(){
        if(flag == -1)
            updateSinglePlayerTop5()
        finish()
    }

    private fun updateSinglePlayerTop5() {
        val db = Firebase.firestore
        val player = LBPlayer(
            0,
            data.level,
            data.currentUser?.photo,
            data.points,
            data.totalTables,
            data.totalTime,
            data.currentUser?.userName
        )

        db.collection(Constants.SP_DB_COLLECTION).add(player)
            .addOnSuccessListener {
                Log.i("UPDATEDB", "addDataToFirestore SinglePlayer: Success")
            }.
            addOnFailureListener { e->
                Snackbar.make(
                    binding.root,
                    getString(R.string.fail_save_firestore),
                    Snackbar.LENGTH_LONG
                ).show()
                Log.i("UPDATEDB", "addDataToFirestore SinglePlayer: ${e.message}")
            }
    }

    suspend fun onTimer(tv: TextView, label: String, onTimeOver: () -> Unit){

        while (true){
            delay(1000)
            CoroutineScope(Dispatchers.Main).launch{
                modelView.decTime()
            }
            if (time <= 0){
                onTimeOver()
                break
            }
        }
    }


    companion object{

        const val STATE = "STATE"
        const val FLAG = "DB_WRITE"

        fun getIntent(context:Context?): Intent {
            val intent = Intent(context, SinglePlayerActivity::class.java)

            return intent
        }

        fun getIntentFromMultiplayer(context:Context?, status :Int): Intent {
            val intent = Intent(context, SinglePlayerActivity::class.java)
            intent.putExtra(STATE, status)
            intent.putExtra(FLAG, 1)
            return intent
        }

    }

}
