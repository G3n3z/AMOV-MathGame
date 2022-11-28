package pt.isec.a2020116565_2020116988.mathgame.data


class User(user: String, photo: String?){

    var userName : String;
    var photo : String?;
    var id : Int;

    init {
        userName = user;
        this.photo = photo;
        id = -1;
    }




}