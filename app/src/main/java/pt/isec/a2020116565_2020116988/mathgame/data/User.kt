package pt.isec.a2020116565_2020116988.mathgame.data

import pt.isec.a2020116565_2020116988.mathgame.State


class User(var userName: String,var photo: String?, var id: Int, var state: State){


    var points : Int = 0;
    var nTables : Int = 0;

    constructor(userName : String, photo: String?) : this(userName, photo, 0, State.OnGame ) {}
    constructor(userName : String, photo: String?, id:Int) : this(userName, photo, id, State.OnGame )


}