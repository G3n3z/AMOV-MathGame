package pt.isec.a2020116565_2020116988.mathgame.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import kotlinx.coroutines.*
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import pt.isec.a2020116565_2020116988.mathgame.logic.ClientLogic
import pt.isec.a2020116565_2020116988.mathgame.logic.LogicGame
import pt.isec.a2020116565_2020116988.mathgame.logic.ServerLogic
import pt.isec.a2020116565_2020116988.mathgame.payload.PlayerMessage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

class MultiplayerModelView(private val data :Data):ViewModel() {

    companion object {
        val SERVER_PORT: Int = 9999
    }

    private var job : Job? = null;

    private val START_DIALOG_TIME = data.START_DIALOG_TIME
    var currentTimeDialog: Int = data.START_DIALOG_TIME

    var _users: MutableLiveData<MutableList<User>> = MutableLiveData(mutableListOf())
    var users: LiveData<MutableList<User>> = _users
        get() = _users

    var players: MutableMap<Int, Player> = mutableMapOf()

    var _connState: MutableLiveData<ConnectionState> = MutableLiveData(data.connState)
    var connectionState: LiveData<ConnectionState> = _connState
        get() = _connState

    private var _mode: MutableLiveData<GameMode> = MutableLiveData(GameMode.CLIENT_MODE)
    var mode: LiveData<GameMode> = _mode
        get() = _mode


    var _level: MutableLiveData<Int> = MutableLiveData(data.level)
    var level: LiveData<Int> = _level
        get() = _level

    var _time: MutableLiveData<Int> = MutableLiveData(data.time)
    var time: LiveData<Int> = _time
        get() = _time

    var _points: MutableLiveData<Int> = MutableLiveData(data.points)
    var points: LiveData<Int> = _points
        get() = _points

    var _nConnections: MutableLiveData<Int> = MutableLiveData(data.nConnections)
    var nConnections: LiveData<Int> = _nConnections
        get() = _nConnections

    var _operations: MutableLiveData<MutableList<Operation>> = MutableLiveData(mutableListOf());
    val operation: LiveData<MutableList<Operation>>
        get() = _operations

    var _state: MutableLiveData<State> = MutableLiveData(data.state);
    val state: LiveData<State>
        get() = _state;

    private var serverSocket: ServerSocket? = null
    var socket: Socket? = null;
    private val socketI: InputStream?
        get() = socket?.getInputStream();
    lateinit var clientOutStream: OutputStream
    var sockets: MutableList<Socket> = mutableListOf();
    private var exit: Boolean = false;
    private var lock: String = ""
    private var thread: Thread? = null

    private var service: LogicGame? = null;

    init {
        _operations.postValue(data.operations);
    }

    fun generateTable(table: Table) {
        //data.generateTable(data.level)
        data.operations = table.operations
        data.maxOperation = table.maxOperation
        data.secondOperation = table.secondOperation
        _operations.postValue(data.operations)
    }

    fun onBackPressed() {
        _state.postValue(State.OnDialogBack)
    }

    fun showAnimationResume() {
        _state.postValue(State.OnDialogResume)
    }


    fun cancelDialog() {
        currentTimeDialog = START_DIALOG_TIME
    }

    fun refreshState() {
        _state.postValue(_state.value)
    }

    fun maxOperationRigth() {
        data.points += 2
        _points.postValue(data.points)

    }

    fun secondOperationRigth(table: Table) {
        data.points += 1
        _points.postValue(data.points)
        generateTable(table)
    }

    fun setCountRightAnswers(i: Int) {
        data.countRightAnswers = i
    }

    fun incCountRightAnswers() {
        data.countRightAnswers++;
    }


    fun decTime() {
        if(data.time <= 0) {
            return
        }
        data.time -= 1
        _time.postValue(data.time)
    }

    fun cancelQuit() {
        _state.postValue(State.OnGame)
    }

    fun setMode(mode: GameMode) {
        _mode.postValue(mode);
        if (service != null) {
            return
        }
        service = if (mode == GameMode.SERVER_MODE) {
            ServerLogic(this, data)
        } else {
            ClientLogic(this, data)
        }
    }

    fun startServer() {
        (service as ServerLogic).startServer()
    }

    //Botao start


    //Cliente

    /**
     * Método para terminar o jogo tanto do lado do servidor como no cliente
     * Fecha os respetivos e espera pela a thread do cliente e termina
     */
    fun stopGame() {
        thread{
            service?.exit()
            service = null
            data.connState = ConnectionState.CONNECTING
            data.state = State.OnGame
            stopJob()
        }
    }


    fun startGameInServer() {
        thread { (service as ServerLogic).startGameInServer() }
    }

    fun startClient(strIP: String, port: Int) {
        (service as ClientLogic).startClient(strIP, port)
    }

    fun swipe(index: Int) {
        thread { service?.onSwipe(index) };
    }

    fun startTimer() {
        if (job == null || job?.isActive == false) {
            CoroutineScope(Dispatchers.IO).async {
                job = launch { onTimer() }
            }
        }
    }
    private suspend fun onTimer(){

        while (true){
            delay(1000)
            CoroutineScope(Dispatchers.Main).async{
                decTime()
            }
            if (data.time <= 0){
                _state.postValue(State.OnGameOver)
                break;
            }
        }
    }

    fun stopJob() {
        job?.cancel()
    }

}
