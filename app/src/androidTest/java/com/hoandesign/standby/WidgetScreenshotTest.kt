package com.hoandesign.standby

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.ui.layout.StandbyCardContainer
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.StandByTheme
import com.hoandesign.standby.ui.theme.StandbyBackground
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Systematic screenshot test that programmatically iterates through every single
 * widget in [StandbyWidgetRegistry], rendering both Compact Bento and Fullscreen
 * Ambient presentations, and exporting high-resolution PNGs for visual auditing.
 */
@RunWith(AndroidJUnit4::class)
class WidgetScreenshotTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private fun saveBitmap(bitmap: Bitmap, filename: String): File {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val primaryDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "standby_widget_catalog")
        if (!primaryDir.exists()) primaryDir.mkdirs()

        val primaryFile = File(primaryDir, filename)
        FileOutputStream(primaryFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        Log.i("WidgetScreenshotTest", "Saved screenshot: ${primaryFile.absolutePath} (${primaryFile.length()} bytes)")

        // Also duplicate to root /sdcard/standby_widget_catalog for reliable Firebase Test Lab pulling
        try {
            val sdcardDir = File("/sdcard/standby_widget_catalog")
            if (!sdcardDir.exists()) sdcardDir.mkdirs()
            val sdcardFile = File(sdcardDir, filename)
            FileOutputStream(sdcardFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        } catch (e: Exception) {
            Log.w("WidgetScreenshotTest", "Fallback /sdcard/standby_widget_catalog write failed: ${e.message}")
        }

        return primaryFile
    }

    @Test
    fun captureAllWidgetsCompact() {
        val currentWidgetState = mutableStateOf<StandbyWidgetId?>(null)

        composeTestRule.setContent {
            val widget = currentWidgetState.value
            if (widget != null) {
                StandByTheme(accentColor = AccentOrange) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StandbyBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        StandbyCardContainer(
                            modifier = Modifier.size(380.dp, 340.dp),
                            cornerRadius = 24.dp
                        ) {
                            StandbyWidgetRegistry.RenderCompact(
                                widgetId = widget,
                                accentColor = AccentOrange,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        for (widget in StandbyWidgetId.entries) {
            currentWidgetState.value = widget
            composeTestRule.waitForIdle()

            val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
            saveBitmap(bitmap, "compact_${widget.name.lowercase()}.png")
        }
    }

    @Test
    fun captureAllWidgetsFullscreen() {
        composeTestRule.activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val currentWidgetState = mutableStateOf<StandbyWidgetId?>(null)

        composeTestRule.setContent {
            val widget = currentWidgetState.value
            if (widget != null) {
                StandByTheme(accentColor = AccentOrange) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(StandbyBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        StandbyWidgetRegistry.RenderFullscreen(
                            widgetId = widget,
                            accentColor = AccentOrange,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        for (widget in StandbyWidgetId.entries) {
            currentWidgetState.value = widget
            composeTestRule.waitForIdle()

            val bitmap = composeTestRule.onRoot().captureToImage().asAndroidBitmap()
            saveBitmap(bitmap, "fullscreen_${widget.name.lowercase()}.png")
        }
    }
}
