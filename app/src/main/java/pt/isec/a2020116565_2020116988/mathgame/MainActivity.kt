package pt.isec.a2020116565_2020116988.mathgame

import android.os.Bundle
import android.os.Environment
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.data.UserViewModel
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding
import pt.isec.a2020116565_2020116988.mathgame.fragments.UserProfile
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var data : Data;
    private lateinit var binding : ActivityMainBinding;
    lateinit var pageAdapter : FragmentStateAdapter;
    val app: Application by lazy { application as Application }

    private val userViewModel: UserViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //data = readDataOfInternalStorage()

        userViewModel.selectedUser.observe(this, Observer { user ->
            data.currentUser = user
        })

        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        pageAdapter = FragmentAdapter(this, binding);
        data = Data()
        loadUserFromFile()
        //Get data of database
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

    private fun loadUserFromFile()
    {
        val map: Map<String, File>? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?.listFiles { file -> file.extension == UserProfile.PROFILE_EXTENSION }
            ?.associateBy { it.nameWithoutExtension }
        if (map == null || map.isEmpty()) {
            Snackbar.make(binding.root, getString(R.string.profile_not_found), Snackbar.LENGTH_LONG)
                .show()
            return
        }
        val username = map.keys.toTypedArray()[map.keys.toTypedArray().lastIndex]
        val filename = map[username]?.absolutePath
        data.currentUser = User(username, filename)

    }

}