package pt.isec.a2020116565_2020116988.mathgame.logic

import android.util.Log
import com.google.gson.Gson
import org.json.JSONObject
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.Message
import pt.isec.a2020116565_2020116988.mathgame.payload.MoveMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.PlayerMessage
import pt.isec.a2020116565_2020116988.mathgame.payload.StatusMessage
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
            var message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)

            data.currentUser?.id = message.user?.id!!
            viewModel._connState.postValue(ConnectionState.WAITING_OTHERS)
        }
        while (keepGoing){
            try {
                Log.i("CLIENT", "Waiting Message")
                json = bufIn.readLine();
                type = JSONObject(json).get("typeOfMessage")
                Log.i("CLIENT", json);

                when(type){
                    TypeOfMessage.STATUS_GAME.name ->{
                        var message  = Gson().fromJson<StatusMessage>(json, StatusMessage::class.java)
                        statusGameMessage(message)
                    }
                    TypeOfMessage.NEW_TABLE.name ->{
                        var message  = Gson().fromJson<StatusMessage>(json, StatusMessage::class.java)
                        newTableMessage(message);
                    }
                    TypeOfMessage.INFO_USER.name ->{
                        var message  = Gson().fromJson<PlayerMessage>(json, PlayerMessage::class.java)
                        infoUserMessage(message)
                    }
                    TypeOfMessage.SWIPE.name ->{
                        var message  = Gson().fromJson<MoveMessage>(json, MoveMessage::class.java)
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

    private fun swipeResponseMessage(message: MoveMessage) {

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
        viewModel._operations.postValue(operations)
    }

    private fun statusGameMessage(message: StatusMessage) {
        //Primeira mensagem, é para comecar
        if (viewModel._connState.value == ConnectionState.WAITING_OTHERS && message.status == State.OnGame){
            viewModel._connState.postValue(ConnectionState.CONNECTION_ESTABLISHED)
        }
        viewModel._state.postValue(message.status)

        if (message.status == State.OnGame){
            val operations : MutableList<Operation>  = message.table!!
            viewModel._operations.postValue(operations)
            viewModel._time.postValue(message.time)
            viewModel._points.postValue(message.points)
            viewModel._level.postValue(message.level)
        }
    }


}