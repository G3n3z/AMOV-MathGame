package pt.isec.a2020116565_2020116988.mathgame.payload

import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.enum.TypeOfMessage

data class PlayerMessage(var type: TypeOfMessage, var user:User?):Message(type) {

}
