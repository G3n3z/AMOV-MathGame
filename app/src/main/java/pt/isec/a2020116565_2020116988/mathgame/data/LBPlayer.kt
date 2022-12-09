package pt.isec.a2020116565_2020116988.mathgame.data

data class LBPlayer(
    var id: Int = 0,
    var level: Int = 0,
    var photo: String? = "",
    var points: Int = 0,
    var totalTables: Int = 0,
    var totalTime: Int = 0,
    var username: String? = ""
){
    fun mapPlayerToLBPlayer(player: Player){
        id = player.id
        level = player.level
        photo = player.user?.photo
        points = player.points
        totalTables = player.totalTables
        totalTime = player.totalTime
        username = player.user?.userName
    }
}