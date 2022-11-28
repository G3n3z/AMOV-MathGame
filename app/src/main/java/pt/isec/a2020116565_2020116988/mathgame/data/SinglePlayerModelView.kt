package pt.isec.a2020116565_2020116988.mathgame.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.State

class SinglePlayerModelView(val data: Data) : ViewModel() {




    private val START_DIALOG_TIME = data.START_DIALOG_TIME
    var currentTimeDialog: Int = data.START_DIALOG_TIME

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

        }else if (data.operations[index] == data.secondOperation){
            secondOperationRigth()
        }

    }

    fun decTime() {
        data.time -=1
        _time.postValue(data.time)
    }

    fun cancelQuit() {
        _state.postValue(State.OnGame)
    }

}