package pt.isec.a2020116565_2020116988.mathgame.dialog

import android.app.Dialog
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import pt.isec.a2020116565_2020116988.mathgame.R

class DialogLevelMultiplayer(context: Context) : Dialog(context) {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_level_multiplayer)
        setWidthPercent(90)
    }

    fun setWidthPercent(percentage: Int) {
        val percent = percentage.toFloat() / 100
        val dm = Resources.getSystem().displayMetrics
        val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
        val percentWidth = rect.width() * percent
        window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
    }
}