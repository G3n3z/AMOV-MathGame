package pt.isec.a2020116565_2020116988.mathgame.logic

import pt.isec.a2020116565_2020116988.mathgame.State

interface LogicGame {
    fun onSwipe(index : Int);
    fun exit(state :State? = null);
    fun timeOver()
    fun closeSockets()

}