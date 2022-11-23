package pt.isec.a2020116565_2020116988.mathgame

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import pt.isec.a2020116565_2020116988.mathgame.data.Data
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivitySinglePlayerBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentGameBinding
import pt.isec.a2020116565_2020116988.mathgame.fragments.GameFragment
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface

class SinglePlayerActivity : AppCompatActivity(), GameActivityInterface {
    lateinit var data: Data;
    val app: Application by lazy { application as Application }
    lateinit var binding : ActivitySinglePlayerBinding
    lateinit var fragment:GameFragment;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        data = app.data;
        data.generateTable(data.level);
        fragment = binding.fragmentGame.getFragment<GameFragment>();

    }

    override fun onStart() {
        super.onStart()

    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (fragment.gestureDetector.onTouchEvent(event!!)){
            return true;
        }
        return super.onTouchEvent(event)
    }


    companion object{

        fun getIntent(context:Context?): Intent {
            val intent = Intent(context, SinglePlayerActivity::class.java);

            return intent;
        }

    }

    override fun swipe(index: Int) {
        Log.i("SinglePlayer res: ", data.operations[index].calcOperation().toString())
    }

}