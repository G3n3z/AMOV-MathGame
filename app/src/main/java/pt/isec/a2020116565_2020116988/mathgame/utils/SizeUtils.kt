package pt.isec.a2020116565_2020116988.mathgame.utils

import android.content.res.Resources
import android.graphics.Rect
import android.view.ViewGroup
import android.view.Window

fun setWidthPercent(percentage: Int, window: Window?) {
    val percent = percentage.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    window?.setLayout(percentWidth.toInt(), ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun setSizePercent(percentage: Int, percentageHeight : Int, window: Window?) {
    val percent = percentage.toFloat() / 100
    val percentH = percentageHeight.toFloat() / 100
    val dm = Resources.getSystem().displayMetrics
    val rect = dm.run { Rect(0, 0, widthPixels, heightPixels) }
    val percentWidth = rect.width() * percent
    val percentHeight = rect.height() * percentH
    window?.setLayout(percentWidth.toInt(), percentHeight.toInt())
}