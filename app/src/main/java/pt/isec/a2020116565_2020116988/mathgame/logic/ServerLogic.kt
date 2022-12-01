package pt.isec.a2020116565_2020116988.mathgame.logic

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.data.Player
import pt.isec.a2020116565_2020116988.mathgame.data.Table
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.Message
import pt.isec.a2020116565_2020116988.mathgame.payload.MoveMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.PlayerMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.StatusMessage
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
    fun startServer() {
        serverSocket = ServerSocket(MultiplayerModelView.SERVER_PORT)

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
                        viewModel._nConnections.postValue(data.nConnections)
//                        viewModel.sockets.add(socket!!)
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
        var keepGoing = true;
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
                    onSwipe(msg, idPlayer);
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
    //Botao start
    fun startGameInServer() {
        synchronized(lock){
            exit = true;
        }
        viewModel._connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        var table = Table()
        table.generateTable()
        tables.add(table)
        data.operations = table.operations
        data.time = Data.START_TIME
        data.level = 1
        data.points = 0
        viewModel._operations.postValue(table.operations)
        viewModel._time.postValue(data.time)
        viewModel._level.postValue(data.level)
        viewModel._points.postValue(data.points)
        viewModel._state.postValue(State.OnGame)

        var message  = StatusMessage(TypeOfMessage.STATUS_GAME, State.OnGame, table.operations,
        0,Data.START_TIME, 1)

        updateNewLevel(data.time, data.points, data.level, 0, table, State.OnGame)

        sendMessageAll(message)
        // Criar tabuleiro e mandar para todos
    }

    private fun onSwipe(message: MoveMessage, idPlayer : Int) {
        var player = players[idPlayer]
        var index = message.index

        if (player == null)
            return;
        //var msg = StatusMessage(TypeOfMessage.SWIPE, State.OnGame)
        if (player.table.operations[index] == player.table.maxOperation){
            player.points +=2
            player.currectRigthAnswers++;
            if (player.currectRigthAnswers == Data.COUNT_RIGHT_ANSWERS){
                player.currectRigthAnswers = 0
                player.state = State.OnDialogPause
                //message.player = PlayerMessage.mapToPlayer()
            }else{
                var table = Table(player.level);
                player.time = newLevelTime(player.time)
            }

        }else if (player.table.operations[index] == player.table.secondOperation){
            var table = Table(player.level);
            player.points +=1
        }
        sendMessage(player.outputStream);
    }

    private fun newLevelTime(time:Int):Int {
        var t = time
        if ((t + 5) <= Data.START_TIME){
            t = time+5
        }else{
            t = Data.START_TIME
        }
        return t;
    }

    private fun sendMessage(outputStream: OutputStream) {

    }
}