package pt.isec.a2020116565_2020116988.mathgame.payload

import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.data.Table
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class StatusMessage(var type: TypeOfMessage, var status : State, var table: MutableList<Operation>?,
                         var points : Int, var time: Int, var level: Int):Message(type)
