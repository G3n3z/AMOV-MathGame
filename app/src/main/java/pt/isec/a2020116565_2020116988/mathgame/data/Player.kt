package pt.isec.a2020116565_2020116988.mathgame.data

import pt.isec.a2020116565_2020116988.mathgame.State
import java.io.OutputStream

class Player(var state: State, var table: Table,
             var numTable : Int, var user: User?,
             var points: Int, var level:Int, var time:Int, var id:Int,
             var currectRigthAnswers : Int, var totalTables :Int, var totalTime: Int,
             var outputStream: OutputStream?) {



}