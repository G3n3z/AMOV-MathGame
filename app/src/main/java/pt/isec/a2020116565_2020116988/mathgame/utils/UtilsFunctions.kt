package pt.isec.a2020116565_2020116988.mathgame.utils

import android.widget.TextView
import kotlinx.coroutines.*


suspend fun onTimer(tv: TextView, label: String, timeStart: Int, onTimeOver: () -> Unit){
    var time = timeStart
    while (true){
        delay(1000)
        time -= 1;
        CoroutineScope(Dispatchers.Main).async{
            tv.text = "${label}: ${time}";
        }
        if (time <= 0){
            onTimeOver()
            break;
        }
    }
}