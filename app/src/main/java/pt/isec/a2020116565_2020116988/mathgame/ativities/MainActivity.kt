package pt.isec.a2020116565_2020116988.mathgame.ativities

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import pt.isec.a2020116565_2020116988.mathgame.Application
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var data : Data;
    private lateinit var binding : ActivityMainBinding;
    lateinit var pageAdapter : FragmentStateAdapter;
    val app: Application by lazy { application as Application }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        data = Data()
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)

        //Get data of database
        loadUserFromFile()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_prefs,menu)
        return true
    }

    @SuppressLint("CommitPrefEdits")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSettings -> {
                val sharedPrefs = getSharedPreferences(SHAREDPREFS, MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                app.data.currentUser = null
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadUserFromFile()
    {
        val sharedPrefs = getSharedPreferences(SHAREDPREFS, MODE_PRIVATE)
        val username = sharedPrefs.getString(PROFILE_USER,"")!!
        val photo = sharedPrefs.getString(PROFILE_PHOTO,"")!!

        if (username.isBlank() || photo.isBlank()) {
            Snackbar.make(binding.root, getString(R.string.profile_not_found), Snackbar.LENGTH_LONG)
                .show()
            return
        }
        Log.i("loadUserFromFile", username)
        app.data.currentUser = User(username, photo)

    }

    companion object{
        const val PROFILE_USER = "profileUsername"
        const val PROFILE_PHOTO = "profilePhoto"
        const val SHAREDPREFS = "ProfileSharedPrefs"
    }

}

