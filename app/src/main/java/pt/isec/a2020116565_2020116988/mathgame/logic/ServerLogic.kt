package pt.isec.a2020116565_2020116988.mathgame.logic

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.json.JSONObject
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.*
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.*
import java.io.BufferedReader
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.concurrent.thread

class ServerLogic(private var viewModel : MultiplayerModelView, var data: Data) : LogicGame {


    private var serverSocket : ServerSocket? = null;
    private var exit : Boolean = false;
    private var lock : String = ""
    private var players = viewModel.players
    private var tables: MutableList<Table> = mutableListOf()
    private var sockets: MutableList<Socket> = mutableListOf()
    private var threads: MutableList<Thread> = mutableListOf()
    private var _nConnections =  viewModel._nConnections;
    private var _connState =   viewModel._connState
    private var _users =   viewModel._users
    private var _operations = viewModel._operations
    private var _time = viewModel._time
    private var _level = viewModel._level
    private var _points = viewModel._points
    private var _state = viewModel._state
    private var exitJob :Boolean = false;
    private var exitThread :Boolean = false;

    private var timerGameOver : Job? = null;
    fun startServer() {
        if (serverSocket != null)
            return
        serverSocket = ServerSocket(MultiplayerModelView.SERVER_PORT)

        thread {
            try {
                var keepGoing = true;
                serverSocket?.soTimeout = 2000;
                players[players.size] =
                    Player(
                        viewModel._state.value!!,
                        Table(),
                        0,
                        data.currentUser,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        0,
                        null
                    );
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
                    threads.add(thread{ startCommunicationWithClient(socket)})
                }
            }
            catch (e: IOException ){
                Log.i("StartServer", e.message.toString())

            }
            Log.i("startServer", "Saiu da server socket")
        }
    }

    private fun startCommunicationWithClient(socket: Socket) {
        val bufOut = socket.getOutputStream()
        val bufI = socket.getInputStream().bufferedReader()
        var idPlayer :Int = -1
        var message: PlayerMessage? = null;
        //Primeira mensagem com os dados
        var json = bufI.readLine();
        var type = JSONObject(json).get("typeOfMessage")

        if (type == TypeOfMessage.INFO_USER.name ){
            message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)
            Log.i("User arrived", message.user?.userName.toString() ?: "Nao consegui imprimeir")
            synchronized(players) {

                message?.user?.id = players.size
                idPlayer = players.size
                players[players.size] = Player(
                    State.OnGame,
                    Table(),
                    0,
                    message?.user,
                    0,
                    0,
                    0,
                    idPlayer,
                    0,
                    0,
                    0,
                    bufOut
                );

            }
        }else{
            Log.i("startCommunication", type.toString())
        }
        if (message != null){
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
        }
        receiveMessageRoutine(bufI, idPlayer, socket);

    }

    private fun receiveMessageRoutine(bufI:BufferedReader, idPlayer : Int, socket: Socket) {
        var keepGoing = true;
        var type : Any;
        var json : String;
        //cilco de leitura
        while (keepGoing){
            try {
                Log.i("startComm", "Waiting message")
                json = bufI.readLine();
                type = JSONObject(json).get("typeOfMessage")
            }catch (e : Exception){
                Log.i("startComm", e.message.toString())
                if(exitThread){
                    break
                }
                else if (players[idPlayer]?.state != State.OnGameOver){
                    if (_connState.value == ConnectionState.CONNECTING){
                        removeUserInConnection(idPlayer);
                    }else{
                        removeUser(PlayerMessage(TypeOfMessage.EXIT_USER, players[idPlayer]?.user), idPlayer);
                        connLost(socket)
                    }
                }
                else{
                    exitUser(idPlayer, socket, PlayerMessage(TypeOfMessage.EXIT_USER, players[idPlayer]?.user))
                }

                break;
            }
            Log.i("Server msg recebida", json)
            when(type){
                TypeOfMessage.INFO_USER.name -> {
                    val msg = Gson().fromJson(json, PlayerMessage::class.java)
                    val id = msg.user?.id;
                    synchronized(players){
                        players[id]?.user = msg.user
                    }
                }
                TypeOfMessage.SWIPE.name -> {
                    val msg = Gson().fromJson(json, MoveMessage::class.java)
                    onSwipeMessage(msg, idPlayer);
                }
                TypeOfMessage.EXIT_USER.name ->{
                    val msg = Gson().fromJson(json, PlayerMessage::class.java)
                    exitUser(idPlayer, socket, msg)
                    keepGoing = false;
                }
                else -> {}
            }
        }
    }

    private fun connLost(socket: Socket){
        socket.close()
        sockets.remove(socket)
        _connState.postValue(ConnectionState.CONNECTION_LOST)
        viewModel.stopJob()
        stopDetector()
    }

    private fun exitUser(idPlayer: Int, socket: Socket, msg:PlayerMessage){
        if (_connState.value == ConnectionState.CONNECTING){
            removeUserInConnection(idPlayer);
        }else{
            removeUser(msg, idPlayer);
            socket.close()
            sockets.remove(socket)
            verifyStatusGame(msg, idPlayer);
        }
    }

    private fun removeUserInConnection(idPlayer: Int) {
        synchronized(players){
            players.remove(idPlayer)
        }
        synchronized(data){
            data.nConnections--
            _nConnections.postValue(data.nConnections)
        }
    }

    private fun verifyStatusGame(message : PlayerMessage, idPlayer: Int) {
        if (countPlayersActive() == 1){
            if (message.user?.state != State.OnGameOver){
                _connState.postValue(ConnectionState.CONNECTION_LOST)
                viewModel.stopJob()
                stopDetector()
                return
            }
        }
        if (allPlayersFinished()){
            startNewLevelAtSeconds(3000, true)

        }else if (allGameFinished()){
            stopDetector()
        }

    }

    private fun countPlayersActive(): Int {
        synchronized(players){
            return players.count { (index, player) -> player.state != State.OnGameOver }
        }
    }

    private fun startNewLevelAtSeconds(time: Int, stop: Boolean) {
        tables.clear()
        if (stop) {
            stopDetector()
        }
        thread{
            Thread.sleep(time.toLong())
            startNewLevel()
        }
    }


    private fun allGameFinished(): Boolean {
        synchronized(players){
            return players.all { (key, value) -> value.state == State.OnGameOver || value.state == State.WINNER }
        }
    }

    private fun removeUser(msg: PlayerMessage, idPlayer: Int):Boolean {

        synchronized(players){
            players[idPlayer]?.user?.state = State.OnGameOver;
            players[idPlayer]?.state = State.OnGameOver;
        }
        var users = _users.value!!
        synchronized(users){
            for (user in users) {
                if (user.id == msg.user?.id){
                  user.state = State.OnGameOver;
                }
            }
        }
        _users.postValue(users);
        sendMessageAll(msg)
        return true
    }

    private fun updateNewLevel(time: Int, points: Int, level: Int, numTable: Int, table: Table, state: State) {
        synchronized(players){
            for (player in players) {
                player.value.numTable = numTable;
                player.value.time = time;
                player.value.level = level;
                player.value.points = points
                player.value.table = table;
                player.value.state = state
            }
        }
    }

    private fun sendMessageAll(message: Message) {
        synchronized(players){
            for (player in players.values) {
                var json = Gson().toJson(message)
                try {
                    player.outputStream?.run {
                        val printStream = PrintStream(this)
                        printStream.println(json)
                        printStream.flush()
                    }
                } catch (e: IOException) {
                    Log.e("sendMessageAll", e.message.toString())
                }
            }
        }
    }
    //Botao start
    fun startGameInServer() {
        synchronized(lock){
            exit = true;
        }
        var message: Message?
        var users = mutableListOf<User>()
        for (player in players) {
            message = PlayerMessage(TypeOfMessage.INFO_USER, player.value.user)
            player.value.user?.let { users.add(it) }
            sendMessageAll(message)
        }
        _users.postValue(users)
        _connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        val table = Table(1)
        tables.add(table)
        data.operations = table.operations
        data.maxOperation = table.maxOperation
        data.secondOperation = table.secondOperation
        data.time = Data.START_TIME
        data.level = 1
        data.points = 0
        _operations.postValue(table.operations)
        _time.postValue(data.time)
        _level.postValue(data.level)
        _points.postValue(data.points)
        _state.postValue(State.OnGame)
        //viewModel.startTimer()
        message = StatusMessage(TypeOfMessage.STATUS_GAME, State.OnGame, table.operations,
        0,Data.START_TIME, 1)

        updateNewLevel(data.time, data.points, data.level, 0, table, State.OnGame)

        sendMessageAll(message)
        startDetector()
        // Criar tabuleiro e mandar para todos
    }

    private fun onSwipeMessage(message: MoveMessage, idPlayer : Int) {
        val player = players[idPlayer]
        val index = message.index

        if (player == null)
            return;

        if (player.table.operations[index] == player.table.maxOperation){
            player.points +=2
            player.totalTables++;
            player.currectRigthAnswers++;
            player.time = message.time
            if (player.currectRigthAnswers == Data.COUNT_RIGHT_ANSWERS){
                if (firstToEndLevel())
                    player.points += 5
                player.state = State.OnDialogPause
                arriveToNextLevel(player)
            }else{
                rightAnswersButNotNextLevel(player)
            }
            updateRecicler(player)
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, null,player.points, player.level, player.id, player.totalTables));
        }else if (player.table.operations[index] == player.table.secondOperation){
            secondRightAnswer(player)
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER,null ,player.points, player.level, player.id, player.totalTables));
            updateRecicler(player)
        }else{
            player.numTable++
            player.totalTables++
            val table : Table = nextTable(player)
            player.table = table
            sendMessage(player.outputStream, SwipeResult(TypeOfMessage.SWIPE,MoveResult.WRONG_OPERATION, table))
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER,null ,player.points, player.level, player.id, player.totalTables));
            updateRecicler(player)
        }
    }

    private fun firstToEndLevel(): Boolean {
        synchronized(players){
            return players.none { (index, player) -> player.state == State.OnDialogPause }
        }

    }

    private fun updateRecicler(player: Player) {
        var users = _users.value!!
        for (user in users) {
            if(user.id == player.id){
                user.points = player.points
                user.state = player.state
                user.nTables = player.totalTables
            }
        }
        _users.postValue(users);
    }

    private fun arriveToNextLevel(player: Player) {
        val msg = StatusMessage(TypeOfMessage.STATUS_GAME, player.state, null, player.points, player.time, player.level)
        sendMessage(player.outputStream, msg)
        if(allPlayersFinished()){
            val msg2 = Message(TypeOfMessage.WILL_START_SOON)
            sendMessageAll(msg2)
            if(_state.value != State.OnGameOver){
                _state.postValue(State.OnDialogResume)
            }
            stopDetector()
            tables.clear()
            thread{
                Thread.sleep(5000)
                startNewLevel()
            }
        }
    }
    private fun nextTable(player: Player):Table{
        val table :Table;
        if(player.numTable < tables.size){
            synchronized(tables){
                table = tables[player.numTable]
            }
        }else{
            table = Table(player.level);
            synchronized(tables){
                tables.add(table)
            }
        }
        return table

    }
    private fun rightAnswersButNotNextLevel(player: Player) {
        player.numTable++
        player.time = newLevelTime(player.time)
        val table : Table = nextTable(player)
        player.table = table
        val msg = StatusMessage(TypeOfMessage.NEW_TABLE, State.OnGame, table.operations, player.points, player.time, player.level,MoveResult.MAX_OPERATION)
        sendMessage(player.outputStream, msg)
    }


    private fun secondRightAnswer(player: Player) {

        player.numTable++
        val table : Table = nextTable(player)
        player.totalTables++;
        player.points +=1
        player.table = table
        val msg = StatusMessage(TypeOfMessage.NEW_TABLE, State.OnGame, table.operations, player.points, player.time, player.level, MoveResult.SECOND_OPERATION)
        sendMessage(player.outputStream, msg)
    }
    private fun startNewLevel() {
        val table = Table(data.level+1)
        tables.add(table)
        if(players[0]?.state != State.OnGameOver) {
            data.operations = table.operations
            data.maxOperation = table.maxOperation
            data.secondOperation = table.secondOperation
            data.time = newLevelTime(data.time)
            data.level++
            _operations.postValue(table.operations)
            _time.postValue(data.time)
            _level.postValue(data.level)
            _points.postValue(data.points)
            _state.postValue(State.OnGame)
        }
        for (player in players) {
            if(player.value.state == State.OnGameOver)
                continue;
            val pl = player.value;
            pl.time = newLevelTime(pl.time)
            pl.numTable = 0
            pl.currectRigthAnswers = 0
            pl.state = State.OnGame
            pl.table = table
            pl.level++
            val message  = StatusMessage(TypeOfMessage.STATUS_GAME, State.OnGame, table.operations,
                pl.points,pl.time, pl.level)
            thread{sendMessage(pl.outputStream, message)}
        }

        startDetector()
    }


    private fun allPlayersFinished(): Boolean {
        synchronized(players){
            return players.none { (index, player) -> player.state == State.OnGame }
        }
    }

    private fun newLevelTime(time:Int):Int {
        var t = time
        t = if ((t + 5) <= Data.START_TIME){
            time+5
            }else{
                Data.START_TIME
            }
        return t;
    }

    private fun sendMessage(outputStream: OutputStream?, msg: Message) {
        outputStream?.run {
            val printStream = PrintStream(this)
            val json = Gson().toJson(msg)
            printStream.println(json)
        }
    }
    private fun updatePlayerServer(table: Table?, state: State) {
        if (table != null) {
            players[0]?.table = table
        }
        players[0]?.totalTables = players[0]?.totalTables?.plus(1) ?: 0
        players[0]?.points = data.points
        players[0]?.level = data.level
        players[0]?.time = data.time
        players[0]?.state = state
        players[0]?.currectRigthAnswers = data.countRightAnswers

        updateRecicler(players[0]!!)
    }
    override fun onSwipe(index : Int){

        if (data.operations[index] == data.maxOperation){
            viewModel.maxOperationRigth()
            viewModel.incCountRightAnswers();
            players[0]?.numTable = players[0]?.numTable?.plus(1)!!
            if (data.countRightAnswers == Data.COUNT_RIGHT_ANSWERS){
                viewModel.setCountRightAnswers(0)
                viewModel.showAnimationPause()
                viewModel.stopJob()
                if (firstToEndLevel()){
                    viewModel.incPoints(5);
                }
                updatePlayerServer(null, State.OnDialogPause)
                if (allPlayersFinished()){
                    val msg2 = Message(TypeOfMessage.WILL_START_SOON)
                    sendMessageAll(msg2)
                    _state.postValue(State.OnDialogResume)
                    tables.clear()
                    stopDetector()
                    thread{
                        Thread.sleep(3000)
                        startNewLevel()
                    }
                }
            }else{
                val table : Table = nextTable(players[0]!!)
                viewModel.generateTable(table)
                updatePlayerServer(table, State.OnGame)
                data.time = newLevelTime(data.time)
                players[0]?.time = data.time
                viewModel._time.postValue(data.time)
            }
            viewModel._moveResult.postValue(MoveResult.MAX_OPERATION);
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER,null, data.points, data.level, data.currentUser?.id!!, players[0]!!.totalTables))
        }else if (data.operations[index] == data.secondOperation){
            players[0]?.numTable = players[0]?.numTable?.plus(1)!!
            val table : Table = nextTable(players[0]!!)
            viewModel.secondOperationRigth(table)
            updatePlayerServer(table, State.OnGame)
            viewModel._moveResult.postValue(MoveResult.SECOND_OPERATION);
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, null, data.points, data.level, data.currentUser?.id!!, players[0]!!.totalTables))
        }else{
            players[0]?.numTable = players[0]?.numTable?.plus(1)!!
            val table : Table = nextTable(players[0]!!)
            viewModel.generateTable(table)
            updatePlayerServer(table, State.OnGame)
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, null, data.points, data.level, data.currentUser?.id!!, players[0]!!.totalTables))
            viewModel._moveResult.postValue(MoveResult.WRONG_OPERATION);
        }

    }

    private fun startDetector(){
        exitJob = false;
        CoroutineScope(Dispatchers.IO).async {
            timerGameOver = launch { detectGameOver(exitJob); }
        }
    }

    private fun stopDetector(){
        exitJob = true;

        if (timerGameOver?.isActive == true){
            timerGameOver?.cancel()
        }
    }

    private suspend fun detectGameOver(exit: Boolean){
        delay(1000)
        Log.i("detectGameOver", "Start")
        var state: State = State.OnGameOver;
        var moreThanOne: Boolean = false;
        while (true){
            val timeStart = System.currentTimeMillis()
            var finished = false
            moreThanOne = false;
            synchronized(players){
                for (player in players) {
                    if (player.value.state == State.OnGame) {
                        player.value.time--
                        player.value.totalTime++
                        if (player.key == 0){
                            data.time = player.value.time
                            _time.postValue(player.value.time)
                        }
                        if (player.value.time <= 0) {
                            player.value.state = State.OnGameOver
                            if(allGameFinished() && !moreThanOne){
                                player.value.state = State.WINNER
                            }
                            if (player.key == 0){
                                _state.postValue(player.value.state)
                            }
                            val msg = UpdateStatusPlayer(TypeOfMessage.GAME_OVER,  player.value.state, player.value.points,
                                player.value.level, player.value.id, player.value.totalTables
                            )
                            finished = true
                            moreThanOne = true;
                            thread{
                                sendMessageAll(msg)
                                updateRecicler(player.value)
                            }
                        }
                    }
                }
            }
            if (finished){
                Log.i("detectGameOver", "GAME OVER")
                if (allGameFinished()){ //Todos perderam
                    updateMultiPlayerTop5()
                    break

                }else if (allPlayersFinished()){ //Todos acabaram o nivel
                    val msg2 = Message(TypeOfMessage.WILL_START_SOON)
                    sendMessageAll(msg2)
                    if(_state.value != State.OnGameOver){
                        _state.postValue(State.OnDialogResume)
                    }
                    startNewLevelAtSeconds(3000, false)
                    break
                }
            }
            if (exit) break;
            val time = 1000 - (System.currentTimeMillis() - timeStart)
            delay(time)
        }
        Log.i("detectGameOver", "Stop")
    }

    override fun exit(state: State?) {
        try {
            exitThread = true
            sockets.forEach{ socket ->
                try {
                    socket.close()
                }catch (_:Exception){}
            }
            serverSocket?.close()
            serverSocket = null
            threads.forEach(Thread::join)
            threads.clear()
            sockets.clear()
            data.nConnections = 0
            players.clear()
            viewModel.players.clear()
            data.clear()
            _users = MutableLiveData(mutableListOf())
            Log.i("exit", _users.value!!.size.toString())
            Log.i("EXIT_SERVER", "Sockets and threads closed")

        }catch (e:IOException){
            Log.i("EXIT_SERVER", e.message.toString())
        }catch (ex:Exception){
            Log.i("EXIT_SERVER", ex.message.toString())
        }


    }

    private fun updateMultiPlayerTop5() {
        val db = Firebase.firestore
        val top5players = mutableListOf<LBPlayer>()
        var sumPoints: Int = 0
        var maxTime: Int = 0

        players.forEach {
            val tempPlayer = LBPlayer()
            tempPlayer.run {
                mapPlayerToLBPlayer(it.value)
            }
            top5players.add(tempPlayer)

            sumPoints += it.value.points

            if (it.value.totalTime > maxTime)
                maxTime = it.value.totalTime
        }

        val game = LBMultiPlayer(points = sumPoints, totalTime = maxTime)

        val docRef = db.collection(Constants.MP_DB_COLLECTION).document()

        docRef.set(game)
            .addOnSuccessListener {
                Log.i("UPDATEDB", "addDataToFirestore Multiplayer: Success")
            }.
            addOnFailureListener { e->
                Log.i("UPDATEDB", "addDataToFirestore Multiplayer: ${e.message}")
            }

        top5players.forEach {
            docRef.collection(Constants.MP_PLAYERS_DB_COLLECTION).add(it)
        }
    }

    override fun timeOver() {
        viewModel._state.postValue(State.OnGameOver)
        synchronized(players){
            players[0]?.time = 0
        }
    }

    override fun closeSockets() {
        try {
            serverSocket?.close()
            sockets.forEach{ socket ->
                try {
                    socket.close()
                }catch (_:Exception){}
            }
            serverSocket = null
            threads.forEach(Thread::join)
        }catch (e:Exception){
            e.printStackTrace()
        }
        threads.clear()
        sockets.clear()
        data.nConnections = 0
        players.clear()
    }

}