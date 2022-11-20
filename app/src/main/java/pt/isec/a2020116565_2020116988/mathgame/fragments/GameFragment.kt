package pt.isec.a2020116565_2020116988.mathgame.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pt.isec.a2020116565_2020116988.mathgame.MainActivity
import pt.isec.a2020116565_2020116988.mathgame.R
import pt.isec.a2020116565_2020116988.mathgame.data.Operation
import pt.isec.a2020116565_2020116988.mathgame.databinding.ActivityMainBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentGameBinding
import pt.isec.a2020116565_2020116988.mathgame.databinding.FragmentHomeBinding


/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment() {

    var level:Int = 1;
    lateinit var binding : FragmentGameBinding;
    lateinit var operations : MutableList<Operation>;
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {

        binding = FragmentGameBinding.inflate(inflater, container, false)
        (activity as MainActivity).generateTable(level);

        return binding.root;
    }

    override fun onStart() {
        super.onStart()
        operations = (activity as MainActivity).data.operations;

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
}