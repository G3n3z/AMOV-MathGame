package pt.isec.a2020116565_2020116988.mathgame.payload

import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class UpdateStatusPlayer(val type : TypeOfMessage, val points : Int, val level: Int, val idUser: Int) : Message(type)
