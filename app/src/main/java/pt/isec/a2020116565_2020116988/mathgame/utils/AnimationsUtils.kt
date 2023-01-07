package pt.isec.a2020116565_2020116988.mathgame.utils

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.appcompat.app.AppCompatActivity


fun vibratePhone(activity: Activity) {

    val vib = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = activity.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        activity.getSystemService(AppCompatActivity.VIBRATOR_SERVICE) as Vibrator
    }
    if (Build.VERSION.SDK_INT >= 26) {
        vib.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        vib.vibrate(200)
    }
}