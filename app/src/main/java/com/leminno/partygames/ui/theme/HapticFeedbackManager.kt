package com.leminno.partygames.ui.theme

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

class HapticFeedbackManager(private val context: Context) {

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Performs a light tactile tick (e.g. tile tap, button press).
     */
    fun performTick(composeHaptics: HapticFeedback? = null) {
        try {
            composeHaptics?.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        } catch (_: Exception) {
            // Fallback to Vibrator API
            vibrateOneShot(10, 50)
        }
    }

    /**
     * Performs a crisp double pop (e.g. card flip, correct guess).
     */
    fun performPop() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timing = longArrayOf(0, 15, 40, 20)
            val amplitudes = intArrayOf(0, 180, 0, 255)
            vibratePattern(timing, amplitudes)
        } else {
            vibrateOneShot(30, 200)
        }
    }

    /**
     * Performs a heavy rumble / explosion burst (e.g. Hot Potato explosion, victory).
     */
    fun performHeavyBurst() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrateOneShot(300, 255)
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(300)
        }
    }

    /**
     * Performs a low rumble warning pulse (e.g. timer final 5s warning, wrong answer thud).
     */
    fun performWarningThud() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val timing = longArrayOf(0, 60, 30, 60)
            val amplitudes = intArrayOf(0, 120, 0, 180)
            vibratePattern(timing, amplitudes)
        } else {
            vibrateOneShot(100, 100)
        }
    }

    private fun vibrateOneShot(milliseconds: Long, amplitude: Int) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(milliseconds, amplitude.coerceIn(1, 255))
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(milliseconds)
                }
            }
        } catch (_: Exception) {
            // Graceful degradation when permission missing or motor unavailable
        }
    }

    private fun vibratePattern(timing: LongArray, amplitudes: IntArray) {
        try {
            if (vibrator?.hasVibrator() == true && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(timing, amplitudes, -1))
            }
        } catch (_: Exception) {
            // Graceful degradation
        }
    }
}
