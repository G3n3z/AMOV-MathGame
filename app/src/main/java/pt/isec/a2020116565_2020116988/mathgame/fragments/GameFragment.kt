package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import pt.isec.a2020116565_2020116988.mathgame.SinglePlayerActivity
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.data.Orientation
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentGameBinding
import pt.isec.a2020116565_2020116988.mathgame.interfaces.GameActivityInterface
import kotlin.math.abs


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment(), GestureDetector.OnGestureListener {

    var level:Int = 1;
    lateinit var binding : FragmentGameBinding;
    lateinit var operations : MutableList<Operation>;
    var cell_width: Int = 0;
    var cell_height: Int = 0;
    val gestureDetector : GestureDetector by lazy {
        GestureDetector(context, this)
    }
    lateinit var gameInterface:GameActivityInterface;

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        //(activity as MainActivity).generateTable(level);
        Log.i("ISEC", "CREATE")

        return binding.root;
    }

    override fun onStart() {
        super.onStart()
        Log.i("ISEC", "START")
        operations = (activity as SinglePlayerActivity).data.operations;

        binding.cell1.text = operations[0].op1.toString();
        binding.cell2.text = operations[0].operator1.toString()
        binding.cell3.text = operations[0].op2.toString();
        binding.cell4.text = operations[0].operator2.toString()
        binding.cell5.text = operations[0].op3.toString()

        binding.cell11.text = operations[1].op1.toString();
        binding.cell12.text = operations[1].operator1.toString()
        binding.cell13.text = operations[1].op2.toString();
        binding.cell14.text = operations[1].operator2.toString()
        binding.cell15.text = operations[1].op3.toString()

        binding.cell21.text = operations[2].op1.toString();
        binding.cell22.text = operations[2].operator1.toString()
        binding.cell23.text = operations[2].op2.toString();
        binding.cell24.text = operations[2].operator2.toString()
        binding.cell25.text = operations[2].op3.toString()

        binding.cell1.text = operations[3].op1.toString();
        binding.cell6.text = operations[3].operator1.toString()
        binding.cell11.text = operations[3].op2.toString();
        binding.cell16.text = operations[3].operator2.toString()
        binding.cell21.text = operations[3].op3.toString()

        binding.cell3.text = operations[4].op1.toString();
        binding.cell8.text = operations[4].operator1.toString()
        binding.cell13.text = operations[4].op2.toString();
        binding.cell18.text = operations[4].operator2.toString()
        binding.cell23.text = operations[4].op3.toString()

        binding.cell5.text = operations[5].op1.toString();
        binding.cell10.text = operations[5].operator1.toString()
        binding.cell15.text = operations[5].op2.toString();
        binding.cell20.text = operations[5].operator2.toString()
        binding.cell25.text = operations[5].op3.toString()


    }



    companion object {

        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            GameFragment().apply {

            }
    }

    override fun onDown(p0: MotionEvent): Boolean {
        Log.i("FLING", "onDown")
        return false;
    }

    override fun onShowPress(p0: MotionEvent) {
        Log.i("FLING", "onShowPress")
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        Log.i("FLING", "onSingleTapUp")
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        Log.i("FLING", "onScroll")
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
        Log.i("FLING", "longpress")
    }

    override fun onFling(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {

        cell_width = binding.cell2.width;
        cell_height =  binding.cell2.height
        val distanceX = p1.x - p0.x
        val distanceY = p1.y - p0.y;
        val initial: IntArray = intArrayOf(0,0);
        binding.cell1.getLocationOnScreen(initial);
        var finalY = (cell_width * 5)+initial[1]

        Log.i("FLING", "Init ${initial[0]} ${initial[1]}")
        if(abs(distanceY) > cell_height && abs(distanceX) > cell_width ||
            (abs(distanceY) < cell_height && abs(distanceX) < cell_width)){
            return false;
        }
        if (p0.y > finalY || p1.y > finalY){
            return true;
        }
        var index = 0;
        var orientation: Orientation;
        if (abs(distanceY) > 100){
            orientation = Orientation.VERTICAL;
            index = ((p0.x-initial[0]) / cell_width).toInt()
            Log.i("FLING", "${p0.x-initial[0]} - ${cell_width*index}, ${cell_width*(index+1)} vertical")
            index = calcColumn(index, orientation)
            if (index < 0) return true;
            Log.i("INDEX", "$index")
            Log.i("Operation", "${operations[index].op1}${operations[index].operator1}" +
                    "${operations[index].op2}${operations[index].operator2}${operations[index].op3}")
        }else if(abs(distanceX) > 100){
            orientation = Orientation.HORIZONTAL;
            index = ((p0.y -initial[1])/cell_height).toInt()
            index = calcColumn(index, orientation)
            if (index < 0) return true;
            Log.i("INDEX", "$index")
            Log.i("FLING", "$index horizontal")
            Log.i("Operation", "${operations[index].op1}${operations[index].operator1}" +
                    "${operations[index].op2}${operations[index].operator2}${operations[index].op3}")
        }
        (activity as GameActivityInterface).swipe(index);
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