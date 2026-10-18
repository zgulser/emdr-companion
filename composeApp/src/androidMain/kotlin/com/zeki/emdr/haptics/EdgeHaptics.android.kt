package com.zeki.emdr.haptics

import android.app.Activity
import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object AndroidHapticsHost {
    @Volatile
    var context: Context? = null

    @Volatile
    var activity: Activity? = null
}

actual fun createEdgeHaptics(): EdgeHaptics = AndroidEdgeHaptics()

/**
 * Wall tick. Flagship LRA phones can play a primitive click; cheap ERM
 * motors (empty HAL capabilities) drop short TOUCH ticks, so those get a
 * brief MEDIA one-shot that actually spins the mass.
 */
private class AndroidEdgeHaptics : EdgeHaptics {
    override fun lightImpact() {
        val vibrator = vibrator()?.takeIf { it.hasVibrator() } ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R &&
            vibrator.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_CLICK)
        ) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_CLICK, 0.7f)
                    .compose(),
            )
            return
        }
        playErmTick(vibrator)
    }

    override fun release() = Unit

    private fun playErmTick(vibrator: Vibrator) {
        val effect = VibrationEffect.createOneShot(ERM_TICK_MS, ERM_AMPLITUDE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            vibrator.vibrate(
                effect,
                VibrationAttributes.Builder()
                    .setUsage(VibrationAttributes.USAGE_MEDIA)
                    .build(),
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(
                effect,
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
        }
    }

    private fun vibrator(): Vibrator? {
        val context = AndroidHapticsHost.context ?: return null
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            context.getSystemService(Vibrator::class.java)
        }
    }

    private companion object {
        const val ERM_TICK_MS = 72L
        const val ERM_AMPLITUDE = 255
    }
}
