package com.hoandesign.standby.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import com.hoandesign.standby.MainActivity

/**
 * Pure evaluation function deciding whether StandBy Android should auto-launch.
 *
 * Requirements:
 * - Device must be actively connected to power/charging.
 * - Device must be oriented in landscape mode.
 * - User must have the "Auto-Launch on Dock" preference enabled.
 */
fun shouldAutoLaunchStandBy(
    isCharging: Boolean,
    isLandscape: Boolean,
    isAutoLaunchEnabled: Boolean
): Boolean = isCharging && isLandscape && isAutoLaunchEnabled

/**
 * BroadcastReceiver triggered by [Intent.ACTION_POWER_CONNECTED].
 *
 * When power is connected and the device is docked in landscape orientation
 * (with auto-launch enabled), automatically launches [MainActivity] with
 * flags [Intent.FLAG_ACTIVITY_NEW_TASK] or [Intent.FLAG_ACTIVITY_CLEAR_TOP].
 */
class ChargingReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_POWER_CONNECTED) return

        val isCharging = true
        val isLandscape = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isAutoLaunchEnabled = prefs.getBoolean(KEY_AUTO_LAUNCH_ON_DOCK, true)

        if (shouldAutoLaunchStandBy(
                isCharging = isCharging,
                isLandscape = isLandscape,
                isAutoLaunchEnabled = isAutoLaunchEnabled
            )
        ) {
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launchIntent)
        }
    }

    companion object {
        const val PREFS_NAME = "standby_settings"
        const val KEY_AUTO_LAUNCH_ON_DOCK = "auto_launch_on_dock"
        const val KEY_NIGHT_MODE_PREFERENCE = "night_mode_preference"
        const val KEY_ACCENT_COLOR = "accent_color"
        const val KEY_LEFT_SLOT_WIDGETS = "left_slot_widgets"
        const val KEY_RIGHT_SLOT_WIDGETS = "right_slot_widgets"

        /**
         * Companion object reference to [com.hoandesign.standby.receiver.shouldAutoLaunchStandBy].
         */
        fun shouldAutoLaunchStandBy(
            isCharging: Boolean,
            isLandscape: Boolean,
            isAutoLaunchEnabled: Boolean
        ): Boolean = com.hoandesign.standby.receiver.shouldAutoLaunchStandBy(
            isCharging = isCharging,
            isLandscape = isLandscape,
            isAutoLaunchEnabled = isAutoLaunchEnabled
        )
    }
}
