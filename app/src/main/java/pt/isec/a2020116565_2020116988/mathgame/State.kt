package pt.isec.a2020116565_2020116988.mathgame

import pt.isec.a2020116565_2020116988.mathgame.enum.GameMode

enum class State {
    OnGame, OnDialogResume, OnDialogPause, OnDialogBack, OnGameOver, WINNER;

    companion object{
        fun gameModeByInteger(i : Int): State {
            return when(i){
                0 -> State.OnGame
                1 -> State.OnDialogResume
                2 -> State.OnDialogPause
                3 -> State.OnDialogBack
                4 -> State.OnGameOver
                else -> {State.OnGame}
            }
        }
    }
}