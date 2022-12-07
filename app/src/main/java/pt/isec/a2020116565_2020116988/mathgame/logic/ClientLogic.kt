package pt.isec.a2020116565_2020116988.mathgame.logic

import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.json.JSONObject
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.*
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.*
import java.io.IOException
import java.io.OutputStream
import java.io.PrintStream
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.concurrent.thread

class ClientLogic(var viewModel : MultiplayerModelView, var data: Data) : LogicGame {


    lateinit var clientOutStream : OutputStream;
    private var socket = viewModel.socket;
    var thread:Thread? = null

    fun swipeClient(index: Int) {
        clientOutStream.run {
            val message = MoveMessage(
                TypeOfMessage.SWIPE,
                index,
                viewModel._time.value!!,
                data.currentUser?.id ?: -1
            )
            val json = Gson().toJson(message);
            val printStream = PrintStream(this);
            printStream.println(json)
            printStream.flush()
        }
    }


    fun startClient(ip: String, serverPort: Int = MultiplayerModelView.SERVER_PORT) {
        if (socket != null || viewModel.connectionState.value != ConnectionState.CONNECTING){
            return;
        }
        thread = thread {
            try {
                val newsocket = Socket()
                newsocket.connect(InetSocketAddress(ip,serverPort),5000)
                viewModel._connState.postValue(ConnectionState.WAITING_OTHERS);
                startCommunication(newsocket);
                Log.i("startClient", "Connectou")
            }catch (e:Exception){
                Log.i("startClient", e.message.toString())
            }
        }
    }

    private fun startCommunication(newsocket: Socket) {
        var idPlayer : Int = 0;
        socket = newsocket;
        clientOutStream = socket!!.getOutputStream()
        clientOutStream.run {
            if (data.currentUser == null)
                data.currentUser = User("", "")
            val msg = PlayerMessage(TypeOfMessage.INFO_USER, data.currentUser)
            val json = Gson().toJson(msg)
            try {
                val printStream = PrintStream(this)
                printStream.println(json)
                printStream.flush()
            }catch (e: IOException){
                Log.i("startCommunication", e.message.toString())
            }
        }
        val bufIn = socket!!.getInputStream().bufferedReader();
        var keepGoing = true;
        var json = bufIn.readLine();

        var type = JSONObject(json).get("typeOfMessage")
        //var ab = Gson().fromJson<Message>(json, Message::class.java)

        Log.i("CLIENT", json);
        if (type == TypeOfMessage.INFO_USER.name){
            val message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)
            idPlayer = message.user?.id!!
            data.currentUser?.id = message.user?.id!!
            viewModel._connState.postValue(ConnectionState.WAITING_OTHERS)
            data.currentUser?.let { viewModel.users.value?.add(it) }
        }
        while (keepGoing){
            try {
                Log.i("CLIENT", "Waiting Message")
                json = bufIn.readLine();
                if (json == null){
                    viewModel._connState.postValue(ConnectionState.CONNECTION_LOST)
                    viewModel.stopJob()
                    return;
                }
                type = JSONObject(json).get("typeOfMessage")
                Log.i("CLIENT", json);

                when(type){
                    TypeOfMessage.STATUS_GAME.name ->{
                        val message  = Gson().fromJson<StatusMessage>(json, StatusMessage::class.java)
                        statusGameMessage(message)
                    }
                    TypeOfMessage.NEW_TABLE.name ->{
                        val message  = Gson().fromJson<StatusMessage>(json, StatusMessage::class.java)
                        newTableMessage(message);
                    }
                    TypeOfMessage.INFO_USER.name ->{
                        val message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)
                        infoUserMessage(message)
                    }
                    TypeOfMessage.SWIPE.name ->{
                        val message  = Gson().fromJson<MoveMessage>(json, MoveMessage::class.java)
                        swipeResponseMessage(message, idPlayer)
                    }
                    TypeOfMessage.EXIT_USER.name ->{
                        val message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)
                        removeUser(message);
                    }
                    TypeOfMessage.POINTS_PLAYER.name ->{
                        val message  = Gson().fromJson<UpdateStatusPlayer>(json, UpdateStatusPlayer::class.java)
                        updateListOfUsers(message);
                    }
                    TypeOfMessage.GAME_OVER.name -> {
                        val message = Gson().fromJson<StatusMessage>(json, StatusMessage::class.java)
                        gameOver(message)
                    }
                }

            }catch (e : IOException){
                Log.i("startCommunication", e.message.toString())
                keepGoing=false
            }
        }

    }

    private fun gameOver(message: StatusMessage) {
        viewModel._state.postValue(message.status)
        viewModel._time.postValue(message.time)
        data.time = message.time
    }

    private fun updateListOfUsers(message: UpdateStatusPlayer?) {
        val users = viewModel.users.value!!
        for (user in users) {
            if (user.id  == message?.idUser){
                user.points = message.points
            }
        }
        viewModel._users.postValue(users);
    }

    private fun removeUser(message: PlayerMessage) {
        val users = viewModel.users.value!!
        var u : MutableList<User> = mutableListOf();
        for (user in users) {
            if (user.id != message.user?.id){
                u.add(user)
            }
        }
        viewModel._users.postValue(u);
    }

    private fun swipeResponseMessage(message: MoveMessage, idPlayer: Int) {

    }

    //Cliente, adicionar outros utilizadores à sua lista para score
    private fun infoUserMessage(message: PlayerMessage) {
        var users: MutableList<User> = viewModel._users.value!!;
        var u = message.user
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
        viewModel._users.postValue(users);
    }

    private fun newTableMessage(message: StatusMessage) {
        val operations : MutableList<Operation>  = message.table!!
        data.operations = message.table!!
        updateData(message.time, message.points, message.level)
        viewModel._operations.postValue(operations)
        viewModel._time.postValue(message.time)
        viewModel._points.postValue(message.points)
        viewModel._level.postValue(message.level)
    }

    private fun statusGameMessage(message: StatusMessage) {
        //Primeira mensagem, é para comecar
        if (viewModel._connState.value == ConnectionState.WAITING_OTHERS && message.status == State.OnGame){
            viewModel._connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        }
        viewModel._state.postValue(message.status)

        if (message.status == State.OnGame){
            val operations : MutableList<Operation>  = message.table!!
            data.operations = operations
            viewModel._operations.postValue(operations)
            viewModel.startTimer()
        }else{
            Log.i("statusGameMessage", "AQUI")
            viewModel.stopJob()
        }

        updateData(message.time, message.points, message.level)
        viewModel._time.postValue(message.time)
        viewModel._points.postValue(message.points)
        viewModel._level.postValue(message.level)
    }

    fun sendMessage(message:Message){
        clientOutStream.run {
            val json = Gson().toJson(message)
            val printStream = PrintStream(this)
            printStream.println(json)
        }
    }
    fun updateData(time:Int, points:Int, level:Int){
        data.time = time
        data.points = points
        data.level = level
    }
    override fun onSwipe(index : Int){
        val message = MoveMessage(TypeOfMessage.SWIPE, index, viewModel._time.value!!, data.currentUser?.id!!)
        thread{sendMessage(message)}
    }

    override fun exit() {
        try {

            val msg = PlayerMessage(
                TypeOfMessage.EXIT_USER,
                User(data.currentUser?.userName!!, null, data.currentUser?.id!!)
            )

            sendMessage(msg)


            socket?.close()
            socket = null
            thread?.join() //TODO tratar o fecho correto da thread
            thread = null
        }catch (e:Exception){
            e.printStackTrace()
            Log.i("CLIENTE", e.message.toString())
        }
    }

}