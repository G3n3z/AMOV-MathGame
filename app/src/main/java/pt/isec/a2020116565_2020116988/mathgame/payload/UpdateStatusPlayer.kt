package pt.isec.a2020116565_2020116988.mathgame.payload

import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class UpdateStatusPlayer(var type : TypeOfMessage, val state : State?, val points : Int, val level: Int,
                              val idUser: Int, val numBoards: Int) : Message(type)
