package com.velmar.othik.downwash.data.audio

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.velmar.othik.downwash.domain.ProgressRepository

class Haptics(context: Context, private val repo: ProgressRepository) {

    private val vibrator: Vibrator =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

    fun buzz(ms: Long) {
        if (!repo.options().vibration || !vibrator.hasVibrator()) return
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun tick() = buzz(14)

    fun knock() = buzz(34)

    fun success() = buzz(70)

    fun error() = buzz(110)
}
