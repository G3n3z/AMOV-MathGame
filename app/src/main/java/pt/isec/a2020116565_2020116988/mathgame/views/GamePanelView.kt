package pt.isec.a2020116565_2020116988.mathgame.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.GridLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.SinglePlayerActivity
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.data.Orientation
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import kotlin.math.abs


class GamePanelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    operations : MutableList<Operation>,
    owner: GameActivityInterface,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes), GestureDetector.OnGestureListener {

    lateinit var operations : MutableList<Operation>
    var cell_width: Int = 0;
    var cell_height: Int = 0;
    lateinit var owner: GameActivityInterface;
    private val gameBoard : GridLayout
        get() = findViewById(R.id.board)
    val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }
    init {
        inflate(context,R.layout.fragment_game,this)
        this.operations = operations
        this.owner = owner
        mount()
        //TODO api 22
        post{
             gameBoard.minimumHeight = (parent as View).height
        }

    }

    fun mount() {

        if (operations.size == 0)
            return;
        findViewById<TextView>(R.id.cell_1).text = operations[0].op1.toString();
        findViewById<TextView>(R.id.cell_2).text = operations[0].operator1.toString()
        findViewById<TextView>(R.id.cell_3).text = operations[0].op2.toString();
        findViewById<TextView>(R.id.cell_4).text = operations[0].operator2.toString()
        findViewById<TextView>(R.id.cell_5).text = operations[0].op3.toString()
        Log.i("AQUI", "Construtor"+findViewById<TextView>(R.id.cell_1).text)
        findViewById<TextView>(R.id.cell_11).text = operations[1].op1.toString();
        findViewById<TextView>(R.id.cell_12).text = operations[1].operator1.toString()
        findViewById<TextView>(R.id.cell_13).text = operations[1].op2.toString();
        findViewById<TextView>(R.id.cell_14).text = operations[1].operator2.toString()
        findViewById<TextView>(R.id.cell_15).text = operations[1].op3.toString()

        findViewById<TextView>(R.id.cell_21).text = operations[2].op1.toString();
        findViewById<TextView>(R.id.cell_22).text = operations[2].operator1.toString()
        findViewById<TextView>(R.id.cell_23).text = operations[2].op2.toString();
        findViewById<TextView>(R.id.cell_24).text = operations[2].operator2.toString()
        findViewById<TextView>(R.id.cell_25).text = operations[2].op3.toString()

        findViewById<TextView>(R.id.cell_1).text = operations[3].op1.toString();
        findViewById<TextView>(R.id.cell_6).text = operations[3].operator1.toString()
        findViewById<TextView>(R.id.cell_11).text = operations[3].op2.toString();
        findViewById<TextView>(R.id.cell_16).text = operations[3].operator2.toString()
        findViewById<TextView>(R.id.cell_21).text = operations[3].op3.toString()

        findViewById<TextView>(R.id.cell_3).text = operations[4].op1.toString();
        findViewById<TextView>(R.id.cell_8).text = operations[4].operator1.toString()
        findViewById<TextView>(R.id.cell_13).text = operations[4].op2.toString();
        findViewById<TextView>(R.id.cell_18).text = operations[4].operator2.toString()
        findViewById<TextView>(R.id.cell_23).text = operations[4].op3.toString()

        findViewById<TextView>(R.id.cell_5).text = operations[5].op1.toString();
        findViewById<TextView>(R.id.cell_10).text = operations[5].operator1.toString()
        findViewById<TextView>(R.id.cell_15).text = operations[5].op2.toString();
        findViewById<TextView>(R.id.cell_20).text = operations[5].operator2.toString()
        findViewById<TextView>(R.id.cell_25).text = operations[5].op3.toString()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gestureDetector.onTouchEvent(event!!)){
            return true
        }
        return super.onTouchEvent(event)
    }
    override fun onDown(p0: MotionEvent): Boolean {
        //Log.i("FLING", "onDown")
        return true;
    }

    override fun onShowPress(p0: MotionEvent) {
        //Log.i("FLING", "onShowPress")
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        //Log.i("FLING", "onSingleTapUp")
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        //Log.i("FLING", "onScroll")
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        //Log.i("FLING", "longpress")
    }


    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.i("FLING", "FLING")
        val tv = findViewById<TextView>(R.id.cell_1)
        cell_width = tv.width;
        cell_height =  tv.height
        val distanceX = p1.x - p0.x
        val distanceY = p1.y - p0.y;
        val initial: IntArray = intArrayOf(0,0);

        if(abs(distanceY) > cell_height && abs(distanceX) > cell_width ||
            (abs(distanceY) < cell_height && abs(distanceX) < cell_width)){
            return false;
        }

        var index = 0;
        var orientation: Orientation;
        if (abs(distanceY) > 100){
            orientation = Orientation.VERTICAL;
            index = (p0.x / cell_width).toInt()
            Log.i("FLING", "${p0.x-initial[0]} - ${cell_width*index}, ${cell_width*(index+1)} vertical")
            index = calcColumn(index, orientation)
            if (index < 0) return true;
            Log.i("INDEX", "$index")
            Log.i("Operation", "${operations[index].op1}${operations[index].operator1}" +
                    "${operations[index].op2}${operations[index].operator2}${operations[index].op3}")
        }else if(abs(distanceX) > 100){
            orientation = Orientation.HORIZONTAL;
            index = (p0.y/cell_height).toInt()
            index = calcColumn(index, orientation)
            if (index < 0) return true;
            Log.i("INDEX", "$index")
            Log.i("FLING", "$index horizontal")
            Log.i("Operation", "${operations[index].op1}${operations[index].operator1}" +
                    "${operations[index].op2}${operations[index].operator2}${operations[index].op3}")
        }

        owner.swipe(index);
        return true;
    }

    private fun calcColumn(index: Int, orientation: Orientation):Int {
        if(index%2 != 0)
            return -1;
        if(orientation == Orientation.HORIZONTAL){
            return index / 2;
        }else{

            return (index / 2) + 3
        }
    }
}