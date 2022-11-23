package pt.isec.a2020116565_2020116988.mathgame.data

import android.media.Image
import android.util.Log

class Data {

    var currentUser: User? = null;
    var operations : MutableList<Operation> = ArrayList(5);
    var level:Int = 1;
    var time: Int = 60;
    var operators : MutableList<Char> = mutableListOf('+', '-')
    lateinit var maxOperator : Operation;


    fun generateTable(level: Int) {
        operations.clear();
        var numbers : MutableList<Int> = ArrayList()
        for(i in 1..9){
            var number = (Math.random() * (10 * level)).toInt()
            if (number == 0) {
                number = 1
            }
            numbers.add(number)
        }

        operations.add(Operation(numbers[0], numbers[1], numbers[2], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[3], numbers[4], numbers[5], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[6], numbers[7], numbers[8], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[0], numbers[3], numbers[7], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[1], numbers[4], numbers[8], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[2], numbers[5], numbers[8], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));

        //operations.forEach{ it -> Log.i("OPERATIONS", "${it.op1}${it.operator1}${it.op2}${it.operator2}${it.op3}") }


    }


}