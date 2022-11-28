package pt.isec.a2020116565_2020116988.mathgame.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UserViewModel : ViewModel() {
    private val mutableSelectedUser = MutableLiveData<User?>()
    val selectedUser : LiveData <User?>
        get() = mutableSelectedUser

    fun selectUser(user: User)
    {
        mutableSelectedUser.value = user
    }
}