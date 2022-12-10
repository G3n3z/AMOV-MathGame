package pt.isec.a2020116565_2020116988.mathgame.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import pt.isec.a2020116565_2020116988.mathgame.R


class ServerModalInitial @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {


    init {
        inflate(context,R.layout.server_modal_initial, this)
    }

    val tvClients : TextView = findViewById(R.id.tvClients);
    val tvIp : TextView = findViewById(R.id.ip_tv);
    val button : Button = findViewById(R.id.btn_start);
    val buttonCancel : Button = findViewById(R.id.btn_cancel_server_modal);


}