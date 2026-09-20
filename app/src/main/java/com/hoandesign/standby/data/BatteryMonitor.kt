package com.hoandesign.standby.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.hoandesign.standby.model.BatteryState
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

/**
 * Pure parsing function that extracts [BatteryState] from an Android battery status intent.
 *
 * Designed as a pure function to allow deterministic, zero-dependency unit testing.
 *
 * @param intent Intent broadcast from [Intent.ACTION_BATTERY_CHANGED], or null.
 * @return Parsed [BatteryState] reflecting percentage, charging state, speed, and time-to-full.
 */
fun parseBatteryIntent(intent: Intent?): BatteryState {
    if (intent == null) {
        return BatteryState()
    }

    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)

    return calculateBatteryState(level, scale, status, plugged)
}

/**
 * Pure calculation function for battery state given raw system metrics.
 */
fun calculateBatteryState(
    level: Int,
    scale: Int,
    status: Int,
    plugged: Int
): BatteryState {
    val percentage = if (level >= 0 && scale > 0) {
        ((level.toFloat() / scale.toFloat()) * 100f).roundToInt().coerceIn(0, 100)
    } else {
        85
    }

    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

    val chargeSpeed = when {
        !isCharging -> "On Battery"
        plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi"
        plugged == BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
        plugged == BatteryManager.BATTERY_PLUGGED_AC -> "Fast Charging"
        else -> "Fast Charging"
    }

    val timeToFullMinutes = if (isCharging) {
        if (percentage >= 100) {
            0
        } else {
            val remainingPct = 100 - percentage
            ((remainingPct * 1.47f).roundToInt()).coerceAtLeast(1)
        }
    } else {
        null
    }

    return BatteryState(
        percentage = percentage,
        isCharging = isCharging,
        chargeSpeed = chargeSpeed,
        capacityMah = 4895,
        timeToFullMinutes = timeToFullMinutes
    )
}

/**
 * Service monitor observing device battery state via sticky broadcast intents.
 */
class BatteryMonitor(
    private val context: Context? = null
) {
    /**
     * Synchronously queries current battery metrics via sticky intent.
     */
    fun getCurrentBatteryState(): BatteryState {
        val ctx = context ?: return BatteryState()
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = ctx.registerReceiver(null, filter)
            parseBatteryIntent(intent)
        } catch (_: Throwable) {
            BatteryState()
        }
    }

    /**
     * Flow that emits real-time battery status changes as broadcasts occur.
     */
    fun batteryStateFlow(): Flow<BatteryState> = callbackFlow {
        val ctx = context
        if (ctx == null) {
            trySend(BatteryState())
            awaitClose { }
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(parseBatteryIntent(intent))
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        try {
            val initialIntent = ctx.registerReceiver(receiver, filter)
            trySend(parseBatteryIntent(initialIntent))
        } catch (_: Throwable) {
            trySend(BatteryState())
        }

        awaitClose {
            try {
                ctx.unregisterReceiver(receiver)
            } catch (_: Throwable) {
                // Ignore if already unregistered
            }
        }
    }
}
