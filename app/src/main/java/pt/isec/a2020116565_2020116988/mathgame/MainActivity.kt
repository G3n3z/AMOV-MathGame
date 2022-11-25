package pt.isec.a2020116565_2020116988.mathgame

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter
import pt.isec.a2020116565_2020116988.mathgame.constants.Constants
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.data.User
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding
import java.io.IOException


class MainActivity : AppCompatActivity() {

    lateinit var data : Data;
    private lateinit var binding : ActivityMainBinding;
    lateinit var pageAdapter : FragmentStateAdapter;
    val app: Application by lazy { application as Application }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //data = readDataOfInternalStorage()
        binding = ActivityMainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        pageAdapter = FragmentAdapter(this, binding);


        //Get data of database
    }

    override fun onStart() {
        super.onStart()
        //Ler de um ficheiro
        data = readDataOfInternalStorage()
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

}