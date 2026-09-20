package com.hoandesign.standby

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.hoandesign.standby.model.NightModePreference
import com.hoandesign.standby.model.NightModeState
import com.hoandesign.standby.receiver.ChargingReceiver
import com.hoandesign.standby.ui.MainStandbyScreen
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.StandByTheme

/**
 * Main StandBy Activity providing full-screen ambient smart display experience.
 *
 * System Features:
 * - Edge-to-edge window insets ([WindowCompat.setDecorFitsSystemWindows]).
 * - Docking awake flag ([WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON]).
 * - Ambient light sensor listener updating [NightModeState.ambientLux].
 * - Transient system bar hiding for immersive OLED standby display.
 */
class MainActivity : ComponentActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null
    private var lightSensor: Sensor? = null
    private var ambientLux by mutableFloatStateOf(50f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Edge-to-edge window insets
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Keep screen awake while docked
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Hide system status and navigation bars
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        // Ambient Light Sensor initialization
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        lightSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Preferences
        val prefs = getSharedPreferences(ChargingReceiver.PREFS_NAME, Context.MODE_PRIVATE)

        setContent {
            val initialPref = remember {
                try {
                    val prefName = prefs.getString(
                        ChargingReceiver.KEY_NIGHT_MODE_PREFERENCE,
                        NightModePreference.DISABLED.name
                    )
                    NightModePreference.valueOf(prefName ?: NightModePreference.DISABLED.name)
                } catch (e: Exception) {
                    NightModePreference.DISABLED
                }
            }

            var nightModeState by remember {
                mutableStateOf(NightModeState.fromLux(ambientLux, initialPref))
            }

            var accentColor by remember {
                val colorHex = prefs.getString(ChargingReceiver.KEY_ACCENT_COLOR, null)
                val initialColor = if (colorHex != null) {
                    try {
                        Color(colorHex.toLong(16))
                    } catch (e: Exception) {
                        AccentOrange
                    }
                } else {
                    AccentOrange
                }
                mutableStateOf(initialColor)
            }

            var autoLaunchOnDock by remember {
                mutableStateOf(prefs.getBoolean(ChargingReceiver.KEY_AUTO_LAUNCH_ON_DOCK, true))
            }

            // Sync sensor lux updates to nightModeState
            LaunchedEffect(ambientLux) {
                nightModeState = nightModeState.withLux(ambientLux)
            }

            StandByTheme(
                nightModeState = nightModeState,
                accentColor = accentColor
            ) {
                MainStandbyScreen(
                    nightModeState = nightModeState,
                    onNightModeStateChange = { updated ->
                        nightModeState = updated
                        prefs.edit().putString(
                            ChargingReceiver.KEY_NIGHT_MODE_PREFERENCE,
                            updated.preference.name
                        ).apply()
                    },
                    accentColor = accentColor,
                    onAccentColorChange = { updatedColor ->
                        accentColor = updatedColor
                        prefs.edit().putString(
                            ChargingReceiver.KEY_ACCENT_COLOR,
                            updatedColor.value.toString(16)
                        ).apply()
                    },
                    autoLaunchOnDock = autoLaunchOnDock,
                    onAutoLaunchOnDockChange = { enabled ->
                        autoLaunchOnDock = enabled
                        prefs.edit().putBoolean(
                            ChargingReceiver.KEY_AUTO_LAUNCH_ON_DOCK,
                            enabled
                        ).apply()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lightSensor?.let { sensor ->
            sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_LIGHT) {
            val lux = event.values.firstOrNull() ?: return
            ambientLux = lux
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
