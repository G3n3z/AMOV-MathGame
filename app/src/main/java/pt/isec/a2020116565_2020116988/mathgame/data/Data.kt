package pt.isec.a2020116565_2020116988.mathgame.data

import android.media.Image
import android.util.Log
import pt.isec.a2020116565_2020116988.mathgame.State
import pt.isec.a2020116565_2020116988.mathgame.enum.ConnectionState

class Data {

    var nConnections: Int = 0
    var countRightAnswers: Int = 0;
    var points: Int = 0;
    var currentUser: User? = null;
    var operations : MutableList<Operation> = ArrayList(5);
    var level:Int = 1;
    var time: Int = START_TIME;
    var totalTime: Int = 0
    var totalTables: Int = 0
    var operators : MutableList<Char> = mutableListOf('+', '-')
    private var OPERATORS : MutableList<Char> = mutableListOf('+', '-', 'x', '/')
    lateinit var maxOperation : Operation;
    lateinit var secondOperation : Operation;
    val START_DIALOG_TIME: Int = 5;
    var state : State = State.OnGame
    var connState: ConnectionState = ConnectionState.CONNECTING

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
        operations.add(Operation(numbers[0], numbers[3], numbers[6], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[1], numbers[4], numbers[7], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));
        operations.add(Operation(numbers[2], numbers[5], numbers[8], operators[(0 until operators.size).random()],
            operators[(0 until operators.size).random()]));

        //operations.forEach{ it -> Log.i("OPERATIONS", "${it.op1}${it.operator1}${it.op2}${it.operator2}${it.op3}") }
        if (level == 2) {
            operators.add(OPERATORS[2])
            operators.add(OPERATORS[3])
        }
        val ordered = operations.toMutableList()
        ordered.sortBy { operation -> operation.calcOperation()}
        maxOperation = ordered[ordered.size-1]
        secondOperation = ordered[ordered.size-2]
        totalTables++
    }

    fun startSinglePlayer() {
        time = START_TIME
        level = 1
        points = 0;
        operators =  mutableListOf('+', '-')
        generateTable(1)

    }

    fun generateMaxOperations() {
        if (operations.size == 0)
            return
        val ordered = operations.toMutableList()
        ordered.sortBy { operation -> operation.calcOperation()}
        maxOperation = ordered[ordered.size-1]
        secondOperation = ordered[ordered.size-2]
    }

    fun clear() {
        operations.clear()
        time = START_TIME
        points = 0;
        level = 1
        currentUser?.points = 0
        currentUser?.nTables = 0
        currentUser?.state = State.OnGame
        totalTime = 0
        countRightAnswers = 0
    }

    companion object {

        const val START_TIME: Int = 60

        const val COUNT_RIGHT_ANSWERS: Int = 3
        const val TIME_TO_START: Long = 5000
    }


}