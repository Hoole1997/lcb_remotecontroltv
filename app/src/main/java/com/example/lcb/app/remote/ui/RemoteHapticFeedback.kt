package com.example.lcb.app.remote.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Provides a short tactile response for remote key presses.
 *
 * Kept behind a small abstraction so the remote page does not need to know
 * about platform-specific vibrator APIs.
 */
class RemoteHapticFeedback(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Vibrator::class.java)
    }

    fun performKeyPress() {
        val deviceVibrator = vibrator?.takeIf { it.hasVibrator() } ?: return
        deviceVibrator.vibrate(keyPressEffect())
    }

    private fun keyPressEffect(): VibrationEffect {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK)
        } else {
            VibrationEffect.createOneShot(KEY_PRESS_DURATION_MS, KEY_PRESS_AMPLITUDE)
        }
    }

    private companion object {
        const val KEY_PRESS_DURATION_MS = 18L
        const val KEY_PRESS_AMPLITUDE = 72
    }
}
