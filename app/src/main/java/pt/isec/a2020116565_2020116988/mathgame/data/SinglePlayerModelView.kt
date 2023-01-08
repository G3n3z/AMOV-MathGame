package pt.isec.a2020116565_2020116988.mathgame.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult

class SinglePlayerModelView(val data: Data) : ViewModel() {


    private val START_DIALOG_TIME = data.START_DIALOG_TIME
    var currentTimeDialog: Int = data.START_DIALOG_TIME

    private var _moveResult: MutableLiveData<MoveResult> = MutableLiveData(MoveResult.NOTHING)
    val moveResult : LiveData<MoveResult>
        get() = _moveResult

    private var _level: MutableLiveData<Int> = MutableLiveData(data.level)
    var level : LiveData<Int> = _level
        get() = _level

    private var _time: MutableLiveData<Int> = MutableLiveData(data.time)
    var time : LiveData<Int> = _time
        get() = _time

    private var _points: MutableLiveData<Int> = MutableLiveData(data.points)
    var points : LiveData<Int>  = _points
        get() = _points

    private var _operations:MutableLiveData<MutableList<Operation>> = MutableLiveData(mutableListOf());
    val operation: LiveData<MutableList<Operation>>
        get() = _operations

    private var _state:MutableLiveData<State> = MutableLiveData(State.OnGame);
    val state : LiveData<State>
        get() = _state;

    var job: Job? = null

    init {
        _operations.postValue(data.operations);
    }

    fun generateTable(){
        data.generateTable(data.level)
        _operations.postValue(data.operations)
    }

    fun onBackPressed() {
        _state.postValue(State.OnDialogBack)
    }

    fun showAnimationResume() {
        _state.postValue(State.OnDialogResume)
    }
    fun showAnimationPause(time: Int) {
        currentTimeDialog = time
        _state.postValue(State.OnDialogPause)
    }
    fun cancelDialog(){
        currentTimeDialog = START_DIALOG_TIME
    }

    fun refreshState() {
        _state.postValue(_state.value)
    }

    fun maxOperationRigth() {
        data.points += 2
        _points.postValue(data.points)

    }

    fun secondOperationRigth() {
        data.points += 1
        _points.postValue(data.points)
        generateTable()
    }

     fun newLevelTime() {
        data.time
        val time = data.time;
        if ((time + 5) <= Data.START_TIME){
            data.time = data.time+5
        }else{
            data.time = Data.START_TIME
        }
        _time.postValue(data.time)
    }

    fun startNewLevel(){
        generateTable()
        newLevelTime()
        data.level+=1
        _level.postValue(data.level)
        _state.postValue(State.OnGame)
    }

    fun setCountRightAnswers(i: Int) {
        data.countRightAnswers = i
    }

    fun incCountRightAnswers() {
        data.countRightAnswers++;
    }

    fun swipe(index: Int) {
        if (data.operations[index] == data.maxOperation){
            maxOperationRigth()
            incCountRightAnswers();
            if (data.countRightAnswers == Data.COUNT_RIGHT_ANSWERS){
                setCountRightAnswers(0)
                showAnimationResume()
            }else{
                generateTable()
                newLevelTime()
            }
            _moveResult.postValue(MoveResult.MAX_OPERATION);
        }else if (data.operations[index] == data.secondOperation){
            secondOperationRigth()
            _moveResult.postValue(MoveResult.SECOND_OPERATION);
        }else{
            generateTable()
            _moveResult.postValue(MoveResult.WRONG_OPERATION);
        }

    }

    fun decTime() {
        if(data.time <= 0)
            return
        data.time -=1
        data.totalTime++
        _time.postValue(data.time)
    }

    fun cancelQuit() {
        _state.postValue(State.OnGame)
    }

    fun onGameOver() {
        _state.postValue(State.OnGameOver)
    }

    fun setState(state: State) {
        _state.postValue(state)
    }

    fun startTimer(){
        if(job == null || job?.isActive == false) {
            Log.i("StartTimer", "On timer")
            job = CoroutineScope(Dispatchers.IO).launch {
                onTimer()
            }
        }
    }

    suspend fun onTimer(){
        while (true){
            delay(1000)
            CoroutineScope(Dispatchers.Main).launch{
                decTime()
            }
            if (data.time <= 0){
                onGameOver()
                break
            }
        }
    }

    fun stopTimer(){
        job?.cancel()
    }

}