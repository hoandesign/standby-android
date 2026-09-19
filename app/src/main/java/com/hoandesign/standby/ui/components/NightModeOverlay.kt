package com.hoandesign.standby.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import com.hoandesign.standby.ui.theme.LocalNightMode
import com.hoandesign.standby.ui.theme.NightRedTint

/**
 * Creates a ColorMatrix transitioning from identity (standard daylight colors)
 * to a monochromatic deep red palette (preserving scotopic night vision)
 * with brightness attenuation.
 *
 * Target red: Color(0xFFFF3B30)
 *
 * @param transition Progress between 0.0f (daylight) and 1.0f (full night mode).
 * @param brightnessScale Brightness factor applied during night mode (e.g. 0.65f).
 */
fun createNightModeColorMatrix(
    transition: Float,
    brightnessScale: Float = 0.65f
): ColorMatrix {
    val t = transition.coerceIn(0f, 1f)
    if (t <= 0.001f) {
        return ColorMatrix()
    }

    // Standard human eye photometric luminance weights (ITU-R BT.709)
    val lumR = 0.2126f
    val lumG = 0.7152f
    val lumB = 0.0722f

    // In deep red night mode, luminance maps directly to deep red with attenuated green & blue
    val targetRedR = lumR * 1.0f * brightnessScale
    val targetRedG = lumG * 1.0f * brightnessScale
    val targetRedB = lumB * 1.0f * brightnessScale

    // Trace green/blue to maintain contrast and legibility without blue glare
    val targetGreenR = lumR * 0.08f * brightnessScale
    val targetGreenG = lumG * 0.08f * brightnessScale
    val targetGreenB = lumB * 0.08f * brightnessScale

    val targetBlueR = lumR * 0.04f * brightnessScale
    val targetBlueG = lumG * 0.04f * brightnessScale
    val targetBlueB = lumB * 0.04f * brightnessScale

    val r00 = (1f - t) + t * targetRedR
    val r01 = t * targetRedG
    val r02 = t * targetRedB

    val r10 = t * targetGreenR
    val r11 = (1f - t) + t * targetGreenG
    val r12 = t * targetGreenB

    val r20 = t * targetBlueR
    val r21 = t * targetBlueG
    val r22 = (1f - t) + t * targetBlueB

    return ColorMatrix(
        floatArrayOf(
            r00, r01, r02, 0f, 0f,
            r10, r11, r12, 0f, 0f,
            r20, r21, r22, 0f, 0f,
            0f,  0f,  0f,  1f, 0f
        )
    )
}

/**
 * Modifier that applies monochromatic red tinting and brightness attenuation
 * when [isNightMode] is true, smoothly animated over 600ms.
 */
fun Modifier.nightModeFilter(
    isNightMode: Boolean,
    brightnessScale: Float = 0.65f
): Modifier = composed {
    val nightTransition by animateFloatAsState(
        targetValue = if (isNightMode) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "nightModeTransition"
    )

    val paint = remember { Paint() }

    this.drawWithContent {
        if (nightTransition > 0.001f) {
            val matrix = createNightModeColorMatrix(nightTransition, brightnessScale)
            paint.colorFilter = ColorFilter.colorMatrix(matrix)

            drawIntoCanvas { canvas ->
                canvas.saveLayer(Rect(0f, 0f, size.width, size.height), paint)
                drawContent()
                canvas.restore()
            }
        } else {
            drawContent()
        }
    }
}

/**
 * Composable container that applies a deep red tint color grade (Color(0xFFFF3B30))
 * and brightness attenuation to all children when [isNightMode] is active.
 *
 * Also provides [LocalNightMode] to its composition subtree.
 */
@Composable
fun NightModeFilterContainer(
    isNightMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalNightMode provides isNightMode) {
        Box(
            modifier = modifier.nightModeFilter(isNightMode)
        ) {
            content()
        }
    }
}
