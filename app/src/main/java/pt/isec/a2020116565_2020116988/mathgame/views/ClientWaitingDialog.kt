package pt.isec.a2020116565_2020116988.mathgame.views

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.Window
import pt.isec.a2020116565_2020116988.mathgame.R

class ClientWaitingDialog(context: Context): Dialog(context) {
    init {
        setCancelable(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.client_wait_dialog)

    }

}