package pt.isec.a2020116565_2020116988.mathgame.data

class Operation(op1:Int, op2: Int, op3: Int, operator1: Char, operator2: Char) {

    val op1:Int;
    val op2:Int;
    val op3:Int;
    val operator1:Char;
    val operator2:Char;


    init {
        this.op1 = op1;
        this.op2 = op2;
        this.op3 = op3;
        this.operator1 = operator1;
        this.operator2 = operator2;
    }

    fun calcOperation(orientation: Operation): Int {
        val firstOperation:Boolean = getPriority();
        if(firstOperation){
            return calc(calc(op1, op2, operator1), op3, operator2);
        }else{
            return calc(op1, calc(op2, op3, operator2) , operator2);
        }

    }

    fun calc(number1:Int, number2:Int, op : Char): Int {
        return when(op){
            'x' -> number1 * number2;
            '/' -> number1 / number2;
            '+' -> number1 + number2
            else -> number1 - number2;
        }
    }

    private fun getPriority(): Boolean {
        if (operator1 == 'x' || operator1 == '/'){
            return true;
        }
        else if(operator2 == 'x' || operator2 == '/'){
            return false;
        }
        return true;
    }
}