package com.example.domain.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/** Vibración corta para mensajes nuevos. */
object VibrationHelper {

    private val PATTERN = longArrayOf(0, 80, 60, 80)

    fun vibrateNewMessage(context: Context) {
        val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (!vibrator.hasVibrator()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(PATTERN, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(PATTERN, -1)
        }
    }
}
