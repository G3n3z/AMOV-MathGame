package pt.isec.a2020116565_2020116988.mathgame.logic

interface LogicGame {
    fun onSwipe(index : Int);
    fun exit();
    fun timeOver()
    fun closeSockets()

}