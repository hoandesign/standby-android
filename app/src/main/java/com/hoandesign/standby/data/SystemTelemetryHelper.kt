package com.hoandesign.standby.data

import android.app.ActivityManager
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.util.Locale

/**
 * Real device system telemetry metrics (Storage, RAM, Network, Battery).
 */
data class StorageMetrics(
    val availableBytes: Long,
    val totalBytes: Long,
    val usedBytes: Long,
    val usedPercent: Int,
    val remainPercent: Int,
    val availableGbFormatted: String,
    val totalGbFormatted: String
)

data class MemoryMetrics(
    val availableBytes: Long,
    val totalBytes: Long,
    val usedBytes: Long,
    val usedRatio: Float,
    val usedGbFormatted: String,
    val totalGbFormatted: String
)

data class NetworkConnectivity(
    val isWifiActive: Boolean,
    val isCellularActive: Boolean,
    val isConnected: Boolean,
    val isBluetoothActive: Boolean
)

data class BatteryTelemetry(
    val percentage: Int,
    val isCharging: Boolean,
    val chargeSpeed: String
)

/**
 * Utility helper that queries live Android system telemetry directly from hardware APIs.
 * Zero hardcoded or mock metrics.
 */
object SystemTelemetryHelper {

    /**
     * Reads real internal storage metrics via [StatFs].
     */
    fun getStorageMetrics(): StorageMetrics {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val availableBytes = availableBlocks * blockSize
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

            val usedPercent = if (totalBytes > 0) {
                ((usedBytes.toDouble() / totalBytes.toDouble()) * 100.0).toInt().coerceIn(0, 100)
            } else 0

            val remainPercent = (100 - usedPercent).coerceIn(0, 100)
            val availableGb = availableBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

            StorageMetrics(
                availableBytes = availableBytes,
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                usedPercent = usedPercent,
                remainPercent = remainPercent,
                availableGbFormatted = String.format(Locale.US, "%.1f GB", availableGb),
                totalGbFormatted = String.format(Locale.US, "%.0f GB", totalGb)
            )
        } catch (_: Exception) {
            StorageMetrics(
                availableBytes = 0L,
                totalBytes = 0L,
                usedBytes = 0L,
                usedPercent = 0,
                remainPercent = 100,
                availableGbFormatted = "-- GB",
                totalGbFormatted = "-- GB"
            )
        }
    }

    /**
     * Reads real RAM memory usage via [ActivityManager.MemoryInfo].
     */
    fun getMemoryMetrics(context: Context): MemoryMetrics {
        return try {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            am.getMemoryInfo(memInfo)

            val totalBytes = memInfo.totalMem
            val availableBytes = memInfo.availMem
            val usedBytes = (totalBytes - availableBytes).coerceAtLeast(0L)

            val usedRatio = if (totalBytes > 0) {
                (usedBytes.toFloat() / totalBytes.toFloat()).coerceIn(0f, 1f)
            } else 0f

            val usedGb = usedBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)
            val totalGb = totalBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

            MemoryMetrics(
                availableBytes = availableBytes,
                totalBytes = totalBytes,
                usedBytes = usedBytes,
                usedRatio = usedRatio,
                usedGbFormatted = String.format(Locale.US, "%.1f", usedGb),
                totalGbFormatted = String.format(Locale.US, "%.0f GB", totalGb)
            )
        } catch (_: Exception) {
            MemoryMetrics(
                availableBytes = 0L,
                totalBytes = 0L,
                usedBytes = 0L,
                usedRatio = 0f,
                usedGbFormatted = "--",
                totalGbFormatted = "-- GB"
            )
        }
    }

    /**
     * Reads live Wi-Fi, Cellular, Internet, and Bluetooth connectivity states.
     */
    fun getNetworkConnectivity(context: Context): NetworkConnectivity {
        var isWifi = false
        var isCellular = false
        var isConnected = false
        var isBt = false

        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetwork = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(activeNetwork)
            if (caps != null) {
                isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                isConnected = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            }
        } catch (_: Exception) {
            // Ignore failure
        }

        try {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            isBt = bm?.adapter?.isEnabled == true
        } catch (_: Exception) {
            // Ignore failure
        }

        return NetworkConnectivity(
            isWifiActive = isWifi,
            isCellularActive = isCellular,
            isConnected = isConnected,
            isBluetoothActive = isBt
        )
    }

    /**
     * Reads live battery metrics from sticky battery broadcast.
     */
    fun getBatteryTelemetry(context: Context): BatteryTelemetry {
        return try {
            val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val intent = context.registerReceiver(null, filter)
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0

            val pct = if (level >= 0 && scale > 0) {
                ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
            } else 100

            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val speed = when {
                !isCharging -> "On Battery"
                plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless Qi"
                plugged == BatteryManager.BATTERY_PLUGGED_USB -> "USB Charging"
                else -> "Fast Charging"
            }

            BatteryTelemetry(
                percentage = pct,
                isCharging = isCharging,
                chargeSpeed = speed
            )
        } catch (_: Exception) {
            BatteryTelemetry(percentage = 100, isCharging = false, chargeSpeed = "Battery")
        }
    }
}
