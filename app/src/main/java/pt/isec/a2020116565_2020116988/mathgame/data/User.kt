package pt.isec.a2020116565_2020116988.mathgame.data


class User(var userName: String,var photo: String?, var id: Int){


    var points : Int = 0;
    var nTables : Int = 0;

    constructor(userName : String, photo: String?) : this(userName, photo, 0 ) {}



}