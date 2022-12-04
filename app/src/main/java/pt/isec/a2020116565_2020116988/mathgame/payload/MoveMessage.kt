package pt.isec.a2020116565_2020116988.mathgame.payload

import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class MoveMessage(var type: TypeOfMessage,
                       var index: Int, var time: Int,var id: Int):Message(type) {
}