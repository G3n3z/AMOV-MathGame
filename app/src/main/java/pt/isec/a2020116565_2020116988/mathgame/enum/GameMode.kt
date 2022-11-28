package pt.isec.a2020116565_2020116988.mathgame.enum

enum class GameMode {
    SERVER_MODE, CLIENT_MODE, FINISH;

    companion object{
        fun gameModeByInteger(i : Int):GameMode{
            return when(i){
                0 -> SERVER_MODE
                1 -> CLIENT_MODE
                else -> FINISH
            }
        }
    }
}