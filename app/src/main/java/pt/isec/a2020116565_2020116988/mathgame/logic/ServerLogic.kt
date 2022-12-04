package pt.isec.a2020116565_2020116988.mathgame.logic

import android.util.Log
import com.google.gson.Gson
import kotlinx.coroutines.delay
import org.json.JSONObject
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.data.Player
import pt.isec.a2020116565_2020116988.mathgame.data.Table
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode
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
    fun startServer() {
        serverSocket = ServerSocket(MultiplayerModelView.SERVER_PORT)

        thread {
            try {
                var keepGoing = true;
                serverSocket?.soTimeout = 10000;
                players[players.size] =
                    Player(viewModel._state.value!!, Table(),0, data.currentUser,0,0,0, 0, 0, null);
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
                        viewModel._nConnections.postValue(data.nConnections)
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
                viewModel._connState.postValue(ConnectionState.CONNECTION_LOST);
            }
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
                players[players.size] = Player(State.OnGame, Table(), 0, message?.user,0,0,0,idPlayer,0, bufOut);

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
        receiveMessageRoutine(bufI, idPlayer);

    }

    private fun receiveMessageRoutine(bufI:BufferedReader, idPlayer : Int) {
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
                break;
            }
            Log.i("Server msg recebida", json)
            when(type){
                TypeOfMessage.INFO_USER.name -> {
                    val msg = Gson().fromJson(json, PlayerMessage::class.java)
                    val id = msg.user?.id;
                    synchronized(viewModel.players){
                        if(viewModel.players.containsKey(id)){
                            viewModel.players.remove(id)
                            data.nConnections--
                            viewModel._nConnections.postValue(data.nConnections)
                        }
                        else
                            viewModel.players[id]?.user = msg.user
                    }
                }
                TypeOfMessage.SWIPE.name -> {
                    val msg = Gson().fromJson(json, MoveMessage::class.java)
                    onSwipeMessage(msg, idPlayer);
                }
                else -> {}
            }
        }
    }

    private fun updateNewLevel(time: Int, points: Int, level: Int, numTable: Int, table: Table, state: State) {
        synchronized(players){
            for (player in players) {
                player.value.numTable = numTable;
                player.value.time = time; player.value.level=level;
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
        for (player in players) {
            message = PlayerMessage(TypeOfMessage.INFO_USER, player.value.user)
            player.value.user?.let { viewModel._users.value?.add(it) }
            sendMessageAll(message)
        }
        viewModel._connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        val table = Table(1)
        tables.add(table)
        data.operations = table.operations
        data.maxOperation = table.maxOperation
        data.secondOperation = table.secondOperation
        data.time = Data.START_TIME
        data.level = 1
        data.points = 0
        viewModel._operations.postValue(table.operations)
        viewModel._time.postValue(data.time)
        viewModel._level.postValue(data.level)
        viewModel._points.postValue(data.points)
        viewModel._state.postValue(State.OnGame)

        message = StatusMessage(TypeOfMessage.STATUS_GAME, State.OnGame, table.operations,
        0,Data.START_TIME, 1)

        updateNewLevel(data.time, data.points, data.level, 0, table, State.OnGame)

        sendMessageAll(message)
        // Criar tabuleiro e mandar para todos
    }

    private fun onSwipeMessage(message: MoveMessage, idPlayer : Int) {
        val player = players[idPlayer]
        val index = message.index

        if (player == null)
            return;

        if (player.table.operations[index] == player.table.maxOperation){
            player.points +=2
            player.currectRigthAnswers++;
            player.time = message.time
            if (player.currectRigthAnswers == Data.COUNT_RIGHT_ANSWERS){
                arriveToNextLevel(player)
            }else{
                rightAnswersButNotNextLevel(player)
            }
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, player.points, player.level, player.id));
        }else if (player.table.operations[index] == player.table.secondOperation){
            secondRightAnswer(player)
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, player.points, player.level, player.id));
        }
    }

    private fun arriveToNextLevel(player: Player) {
        player.state = State.OnDialogPause
        val msg = StatusMessage(TypeOfMessage.STATUS_GAME, player.state, null, player.points, player.time, player.level)
        sendMessage(player.outputStream, msg)
        if(allPlayersFinished()){
            tables.clear()
            thread{
                Thread.sleep(3000)
                startNewLevel()
            }
        }
    }
    private fun nextTable(player: Player):Table{
        val table :Table;
        if(player.numTable < tables.size-1){
            table =  tables[player.numTable]
        }else{
            table = Table(player.level);
            tables.add(table)
        }
        return table

    }
    private fun rightAnswersButNotNextLevel(player: Player) {
        player.numTable++
        player.time = newLevelTime(player.time)
        val table : Table = nextTable(player)
        player.table = table
        val msg = StatusMessage(TypeOfMessage.NEW_TABLE, State.OnGame, table.operations, player.points, player.time, player.level)
        sendMessage(player.outputStream, msg)
    }


    private fun secondRightAnswer(player: Player) {

        val table : Table = nextTable(player)
        player.points +=1
        player.numTable++
        player.table = table
        //player.time = newLevelTime(player.time)
        val msg = StatusMessage(TypeOfMessage.NEW_TABLE, State.OnGame, table.operations, player.points, player.time, player.level)
        sendMessage(player.outputStream, msg)
    }
    private fun startNewLevel() {
        val table = Table(data.level+1)
        tables.add(table)
        data.operations = table.operations
        data.maxOperation = table.maxOperation
        data.secondOperation = table.secondOperation
        data.time = newLevelTime(data.time)
        data.level++
        viewModel._operations.postValue(table.operations)
        viewModel._time.postValue(data.time)
        viewModel._level.postValue(data.level)
        viewModel._points.postValue(data.points)
        viewModel._state.postValue(State.OnGame)

        for (player in players) {
            val pl = player.value;
            pl.time = newLevelTime(pl.time)
            pl.numTable = 0
            pl.currectRigthAnswers = 0
            pl.state = State.OnGame
            pl.table = table
            pl.level++
            val message  = StatusMessage(TypeOfMessage.STATUS_GAME, State.OnGame, table.operations,
                pl.points,pl.time, pl.level)
            sendMessage(pl.outputStream, message)
        }
    }

    private fun allPlayersFinished(): Boolean {
        return players.none { (index, player) -> player.state == State.OnGame }
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

    private fun sendMessage(outputStream: OutputStream?, msg: StatusMessage) {
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
        players[0]?.points = data.points
        players[0]?.level = data.level
        players[0]?.time = data.time
        players[0]?.state = state
        players[0]?.currectRigthAnswers = data.countRightAnswers
        players[0]?.numTable = players[0]?.numTable?.plus(1)!!;

    }
    override fun onSwipe(index : Int){

        if (data.operations[index] == data.maxOperation){
            viewModel.maxOperationRigth()
            viewModel.incCountRightAnswers();

            if (data.countRightAnswers == Data.COUNT_RIGHT_ANSWERS){
                viewModel.setCountRightAnswers(0)
                viewModel.showAnimationResume()
                updatePlayerServer(null, State.OnDialogPause)
                if (allPlayersFinished()){
                    tables.clear()
                    thread{
                        Thread.sleep(3000)
                        startNewLevel()
                    }
                }
            }else{
                val table : Table = nextTable(players[0]!!)
                viewModel.generateTable(table)
                updatePlayerServer(table, State.OnGame)
            }
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, data.points, data.level, data.currentUser?.id!!));
        }else if (data.operations[index] == data.secondOperation){
            val table : Table = nextTable(players[0]!!)
            viewModel.secondOperationRigth(table)
            updatePlayerServer(table, State.OnGame)
            sendMessageAll(UpdateStatusPlayer(TypeOfMessage.POINTS_PLAYER, data.points, data.level, data.currentUser?.id!!));
        }

    }

    override fun exit() {
        try {
//            synchronized(players){
//                for (player in players)
//                    player.value.outputStream?.close()
                //TODO fechar thread de server?
//            }
            sockets.forEach(Socket::close)
            serverSocket?.close()
            serverSocket = null
            threads.forEach(Thread::join)
        }catch (e:IOException){
            Log.i("EXIT_SERVER", e.message.toString())
        }catch (ex:Exception){
            Log.i("EXIT_SERVER", ex.message.toString())
        }

        Log.i("EXIT_SERVER", "Sockets and threads closed")

    }

}