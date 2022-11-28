package pt.isec.a2020116565_2020116988.mathgame.data

import pt.isec.a2020116565_2020116988.mathgame.State
import java.io.OutputStream
import java.io.PrintStream
import kotlin.concurrent.thread

class Player(var state: State, val table: MutableList<Operation?>,
            var maxOperation: Operation, var secondOperation: Operation,
             var numTable : Int, var user: User?,
             var points: Int, var level:Int, var time:Int, var id:Int,
            var outputStream: OutputStream) {


    fun sendMessage(message: MultiplayerModelView.Message){
        outputStream.run {
            thread {
                try {
                    val printStream = PrintStream(this)
                    printStream.println(message)
                    printStream.flush()
                } catch (_: Exception) {
                    //stopGame()
                }
            }
        }
    }

}