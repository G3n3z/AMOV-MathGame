package pt.isec.a2020116565_2020116988.mathgame

import pt.isec.a2020116565_2020116988.mathgame.data.MultiplayerModelView
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

class ThreadClient(var socket: Socket) : Thread(){

    private var inputStream: InputStream = socket.getInputStream();
    private var outputStream: OutputStream = socket.getOutputStream();


    override fun run() {
        super.run()
    }



}