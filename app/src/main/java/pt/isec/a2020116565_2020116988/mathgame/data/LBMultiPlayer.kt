package pt.isec.a2020116565_2020116988.mathgame.data

/**
 * class que representa um jogo multiplayer
 */
data class LBMultiPlayer(
    var points: Int = 0,
    var totalTime: Int = 0,
    var players: MutableMap<String, LBPlayer>
)