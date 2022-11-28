package pt.isec.a2020116565_2020116988.mathgame.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class MultiplayerModelView(private val data :Data):ViewModel() {

    val SERVER_PORT: Int = 9999
    private val START_DIALOG_TIME = data.START_DIALOG_TIME
    var currentTimeDialog: Int = data.START_DIALOG_TIME

    //var connectionState : ConnectionState = ConnectionState.CONNECTING

    private var players : MutableMap<Int,Player> = mutableMapOf()
    private var tables : MutableMap<Int, MutableList<Table>> = mutableMapOf<Int, MutableList<Table>>()
    private var _connState: MutableLiveData<ConnectionState> = MutableLiveData(ConnectionState.CONNECTING)
    var connectionState : LiveData<ConnectionState> = _connState
        get() = _connState

    private var _mode: MutableLiveData<GameMode> = MutableLiveData(GameMode.CLIENT_MODE)
    var mode : LiveData<GameMode> = _mode
        get() = _mode


    private var _level: MutableLiveData<Int> = MutableLiveData(data.level)
    var level : LiveData<Int> = _level
        get() = _level

    private var _time: MutableLiveData<Int> = MutableLiveData(data.time)
    var time : LiveData<Int> = _time
        get() = _time

    private var _points: MutableLiveData<Int> = MutableLiveData(data.points)
    var points : LiveData<Int> = _points
        get() = _points

    private var _nConnections: MutableLiveData<Int> = MutableLiveData(data.nConnections)
    var nConnections : LiveData<Int> = _nConnections
        get() = _nConnections

    private var _operations: MutableLiveData<MutableList<Operation>> = MutableLiveData(mutableListOf());
    val operation: LiveData<MutableList<Operation>>
        get() = _operations

    private var _state: MutableLiveData<State> = MutableLiveData(State.OnGame);
    val state : LiveData<State>
        get() = _state;

    private var serverSocket: ServerSocket? = null
    private var socket : Socket? = null;
    private val socketI : InputStream?
        get() = socket?.getInputStream();

    private var sockets : MutableList<Socket>  = mutableListOf();
    private var exit : Boolean = false;
    private var lock : String = ""
    private var thread : Thread? = null
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

    fun setMode(mode: GameMode) {
        _mode.postValue(mode);
    }

    fun startServer() {
        serverSocket = ServerSocket(SERVER_PORT)
        Log.i("ServerSocket" , serverSocket?.inetAddress?.hostAddress.toString())
        Thread {
            try {
                var keepGoing = true;
                serverSocket?.soTimeout = 10000;
                while (keepGoing) {
                    var socket : Socket? = null
                    try {

                        socket = serverSocket!!.accept();

                            synchronized(lock){
                            if (exit){
                                keepGoing = false;
                            }
                        }
                        Log.i("THREAD", "CHEGOU CLIENTE")
                        data.nConnections +=1
                        _nConnections.postValue(data.nConnections)
                        sockets.add(socket!!)
                    }catch (e : SocketTimeoutException){
                        Log.i("StartServer", "Timeout")
                        continue
                    }
                    thread{ startCommunicationWithClient(socket)}
                }
            }catch (e:IOException ){
                Log.i("StartServer", "Timeout")
                _connState.postValue(ConnectionState.TIMEOUT);
            }
            catch (_:IOException ){
                _connState.postValue(ConnectionState.CONNECTION_LOST);
            }
        }
    }

    private fun startCommunicationWithClient(socket: Socket) {
        val bufOut = socket.getOutputStream()
        val bufI = socket.getInputStream().bufferedReader()
        //Primeira mensagem com os dados
        var json = bufI.readLine();
        var message  = Gson().fromJson<Message>(json,Message::class.java)
        if (message.type == TypeOfMessage.INFO_USER ){
            Log.i("User arrived", message.player?.user?.userName.toString() ?: "Nao consegui imprimeir")
        }else{
            Log.i(message.type.toString(), message.player?.state.toString())
        }
        synchronized(players) {
            if (message.player != null){
                message.player?.id = players.size
                players[players.size] = PlayerMessage.mapToPlayer(message.player!!, bufOut);
            }
        }
        var keepGoing = true;

        //cilco de leitura
        while (keepGoing){
            try {
                json = bufI.readLine();
                message = Gson().fromJson(json, Message::class.java)
            }catch (e : Exception){
                Log.i("startComm", e.message.toString())
                break;
            }
            when(message.type){
            TypeOfMessage.INFO_USER -> {
                val id = message.player?.id;
                synchronized(players){
                    players[id]?.user = message.player?.user
                }
            }
            TypeOfMessage.SWIPE -> {
                //checkJogada
                Log.i("SWIPE", "CHECK JOGADA")
            }
            else -> {

            }
            }
        }

    }

    fun startGameInServer() {
        synchronized(lock){
            exit = true;
        }
    }

    //Cliente

    fun startClient(ip: String, serverPort: Int = SERVER_PORT) {
        if (socket != null || connectionState.value != ConnectionState.CONNECTING){
            return;
        }
        thread = thread {
            try {
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(ip,serverPort),5000)
                _connState.postValue(ConnectionState.WAITING_OTHERS);
                startCommunication(newsocket);

            }catch (e:Exception){
                Log.i("startClient", e.message.toString())
            }
        }
    }

    private fun startCommunication(newsocket: Socket) {
        socket = newsocket;
        val bufOut = socket!!.getOutputStream()
        bufOut.run {
            Message(TypeOfMessage.INFO_USER, PlayerMessage(_state.value, null, data.currentUser, 0, 0, 0, 0))
        }
        val bufIn = socket!!.getInputStream().bufferedReader();
        var keepGoing = true;
        while (keepGoing){
            try {
                Log.i("CLIENT", "Waiting Message")
                var json = bufIn.readLine();

            }catch (e : IOException){
                Log.i("startCommunication", e.message.toString())
            }
        }

    }



    data class Message(var type: TypeOfMessage, var player : PlayerMessage? ){

    }


    data class PlayerMessage(var state: State?, val table: MutableList<Operation?>?, var user:User?,
                    var points: Int, var level:Int, var time:Int, var id:Int) {

        companion object{
            fun mapToPlayer(message :PlayerMessage, out: OutputStream):Player{
                return Player(State.OnGame, mutableListOf(), Operation(), Operation(),0, message.user,
                0, 0, 0, message.id, out)
            }
        }
    }

//    data class User(var username:String, var image:String){
//
//    }
}