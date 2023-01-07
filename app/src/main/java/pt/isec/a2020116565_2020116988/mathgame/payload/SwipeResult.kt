package pt.isec.a2020116565_2020116988.mathgame.payload;

import pt.isec.a2020116565_2020116988.mathgame.enum.MoveResult
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class SwipeResult(val tp: TypeOfMessage, val moveResult: MoveResult) :Message(tp) {
}
