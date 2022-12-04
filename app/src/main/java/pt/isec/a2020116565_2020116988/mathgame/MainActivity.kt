package pt.isec.a2020116565_2020116988.mathgame

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding
import pt.isec.a2020116565_2020116988.mathgame.fragments.UserProfile
import java.io.IOException

class MainActivity : AppCompatActivity() {

    lateinit var data : Data;
    private lateinit var binding : ActivityMainBinding;
    lateinit var pageAdapter : FragmentStateAdapter;
    val app: Application by lazy { application as Application }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //data = readDataOfInternalStorage()

        data = Data()
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        pageAdapter = FragmentAdapter(this, binding);
        //Get data of database
        loadUserFromFile()
    }

    override fun onStart() {
        super.onStart()
        //Ler de um ficheiro
        //data = readDataOfInternalStorage()
    }

    private fun readDataOfInternalStorage():Data{
        val data : Data = Data();
        data.currentUser = null;
        val index = 0;
        try {
            data.currentUser = User("", "");
            applicationContext.openFileInput( Constants.USER_FILE)?.bufferedReader().use {
                it?.forEachLine {

                }
            }


        } catch (e: IOException) {
            return data;
        }
        return data;
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_prefs,menu)
        return true
    }

    @SuppressLint("CommitPrefEdits")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menuSettings -> {
                val sharedPrefs = getSharedPreferences(UserProfile.SHAREDPREFS, MODE_PRIVATE)
                sharedPrefs.edit().clear().apply()
                app.data.currentUser = null
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadUserFromFile()
    {
        val sharedPrefs = getSharedPreferences(UserProfile.SHAREDPREFS, MODE_PRIVATE)
        val username = sharedPrefs.getString(getString(R.string.profileUsername),"")!!
        val photo = sharedPrefs.getString(getString(R.string.profilePhoto),"")!!

        if (username.isBlank() || photo.isBlank()) {
            Snackbar.make(binding.root, getString(R.string.profile_not_found), Snackbar.LENGTH_LONG)
                .show()
            return
        }
        Log.i("loadUserFromFile", username)
        app.data.currentUser = User(username, photo)

    }

}

