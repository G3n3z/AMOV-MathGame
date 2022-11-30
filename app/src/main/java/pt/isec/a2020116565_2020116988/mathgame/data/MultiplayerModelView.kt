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
import java.io.PrintStream
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
    private var tables: MutableList<Table> = mutableListOf()
    private var _users: MutableLiveData<MutableList<User>> = MutableLiveData()
    var users : LiveData<MutableList<User>> = _users
        get() = _users

    private var players : MutableMap<Int,Player> = mutableMapOf()

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
        thread {
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
                        synchronized(lock){
                            if (exit){
                                keepGoing = false;
                            }
                        }
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
        json = Gson().toJson(message)
        bufOut.run {
            try {
                val printStream = PrintStream(this)
                printStream.println(json)
                printStream.flush()
            }catch (e:IOException){
                Log.i("startCommunication", e.message.toString())
            }
        }
        var keepGoing = true;
        //cilco de leitura
        while (keepGoing){
            try {
                Log.i("startComm", "Waiting message")
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
            else -> {}
            }
        }

    }
    //Botao start
    fun startGameInServer() {
        synchronized(lock){
            exit = true;
        }
        _connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        var table: Table = Table()
        table.generateTable()
        tables.add(table)
        data.operations = table.operations
        data.time = Data.START_TIME
        data.level = 1
        data.points = 0
        _operations.postValue(table.operations)
        _time.postValue(data.time)
        _level.postValue(data.level)
        _points.postValue(data.points)
        _state.postValue(State.OnGame)

        var message  = Message(TypeOfMessage.STATUS_GAME, PlayerMessage(State.OnGame, table.operations, null,0, 1,
            Data.START_TIME, 0))

        thread{sendMessageAll(message)}
        // Criar tabuleiro e mandar para todos
    }

    private fun sendMessageAll(message: MultiplayerModelView.Message) {
        synchronized(players){
            for (player in players.values) {
                var out = player.outputStream
                var json = Gson().toJson(message)
                try {
                    out.run {
                        val printStream  = PrintStream(this)
                        printStream.println(json)
                        printStream.flush()
                    }
                }catch (e : IOException){
                    Log.e("sendMessageAll", e.message.toString())
                }
            }
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
                Log.i("startClient", "Connectou")
            }catch (e:Exception){
                Log.i("startClient", e.message.toString())
            }
        }
    }

    private fun startCommunication(newsocket: Socket) {
        socket = newsocket;
        val bufOut = socket!!.getOutputStream()
        bufOut.run {
            val msg = Message(TypeOfMessage.INFO_USER, PlayerMessage(_state.value, null, data.currentUser, 0, 0, 0, 0))
            val json = Gson().toJson(msg)
            try {
                val printStream = PrintStream(this)
                printStream.println(json)
                printStream.flush()
            }catch (e:IOException){
                Log.i("startCommunication", e.message.toString())
            }
        }
        val bufIn = socket!!.getInputStream().bufferedReader();
        var keepGoing = true;
        var json = bufIn.readLine();
        Log.i("CLIENT", json);
        var message  = Gson().fromJson<Message>(json, Message::class.java)
        if (message.type == TypeOfMessage.INFO_USER){
            if (data.currentUser == null)
                data.currentUser = User("", "")
            data.currentUser?.id = message.player?.id!!
            _connState.postValue(ConnectionState.WAITING_OTHERS)
        }
        while (keepGoing){
            try {
                Log.i("CLIENT", "Waiting Message")
                json = bufIn.readLine();
                Log.i("CLIENT", json);
                message  = Gson().fromJson<Message>(json, Message::class.java)
                when(message.type){
                    TypeOfMessage.STATUS_GAME ->{
                        statusGameMessage(message)
                    }
                    TypeOfMessage.NEW_TABLE ->{
                        newTableMessage(message);
                    }
                    TypeOfMessage.INFO_USER ->{
                        infoUserMessage(message)
                    }
                    TypeOfMessage.SWIPE ->{
                        swipeResponseMessage(message)
                    }
                }

            }catch (e : IOException){
                //TODO: Passar para single player
                Log.i("startCommunication", e.message.toString())
                keepGoing=false
            }
        }

    }

    private fun swipeResponseMessage(message: MultiplayerModelView.Message?) {

    }

    //Cliente, adicionar outros utilizadores à sua lista para score
    private fun infoUserMessage(message: MultiplayerModelView.Message?) {
        var users: MutableList<User> = _users.value!!;
        var u = message?.player?.user
        var found = false;
        for (user in users) {
            if (user.id == u?.id){
                user.userName = u.userName
                user.photo = u.photo
                found = true
            }
        }
        if (!found){
            users.add(u!!);
        }
        _users.postValue(users);
    }

    private fun newTableMessage(message: MultiplayerModelView.Message) {
        val operations : MutableList<Operation>  = message.player?.table!!
        _operations.postValue(operations)
    }

    private fun statusGameMessage(message: MultiplayerModelView.Message) {
        if (message.player?.state != null)
            _state.postValue(message.player?.state!!)
        if (message.player?.state == State.OnGame){
            val operations : MutableList<Operation>  = message.player?.table!!
            _operations.postValue(operations)

            //_state.postValue(message.player?.state!!)

            if (_connState.value == ConnectionState.WAITING_OTHERS){
                _connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
            }
        }
    }

    /**
     * Método para terminar o jogo tanto do lado do servidor como no cliente
     * Fecha os respetivos e espera pela a thread do cliente e termina
     */
    fun stopGame() {
        try {
            if (_mode.value == GameMode.SERVER_MODE){
                val message = Message(
                    TypeOfMessage.STATUS_GAME,
                    PlayerMessage(
                        State.OnGameOver,
                        null,
                        null,
                        0,
                        0,
                        0,
                        0)
                )
                sendMessageAll(message)
                serverSocket?.close()
                serverSocket = null
            }
            else if(_mode.value == GameMode.CLIENT_MODE){
                val message = null //TODO: verificar o tipo correto de mensagem a enviar
                socket?.close()
                socket = null
                thread?.join() //TODO tratar o fecho correto da thread
                thread = null
            }
        }catch (e:Exception){
            Log.e("stopGame", e.message.toString())
        }
    }


    data class Message(var type: TypeOfMessage, var player : PlayerMessage? ){
    }


    data class PlayerMessage(var state: State?, val table: MutableList<Operation>?, var user:User?,
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