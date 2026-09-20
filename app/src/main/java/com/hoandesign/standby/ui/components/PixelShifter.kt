package com.hoandesign.standby.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sin

/**
 * Calculates a 2D offset (dx, dy) in dp along a smooth Lissajous curve.
 *
 * Guarantees:
 * 1. Both dx and dy are strictly clamped within [-amplitudeDp, amplitudeDp].
 * 2. Consecutive step indices produce distinct offsets to guarantee
 *    continuous OLED subpixel migration and prevent static burn-in.
 *
 * @param stepIndex The current discrete time step (e.g. currentTimeMillis / 120_000L).
 * @param amplitudeDp Maximum displacement in dp.
 * @return Pair of (dx, dy) offsets in dp.
 */
fun calculatePixelShiftOffset(stepIndex: Long, amplitudeDp: Float = 1.5f): Pair<Float, Float> {
    // Wrap to 8-step cycle, handling negative values safely
    val step = ((stepIndex % 8L) + 8L) % 8L
    val t = step.toDouble() * (2.0 * Math.PI / 8.0)

    // Lissajous curve with frequency ratio a=1, b=2 and phase delta = PI / 4
    val rawDx = (amplitudeDp * sin(t + Math.PI / 4.0)).toFloat()
    val rawDy = (amplitudeDp * sin(2.0 * t)).toFloat()

    val dx = rawDx.coerceIn(-amplitudeDp, amplitudeDp)
    val dy = rawDy.coerceIn(-amplitudeDp, amplitudeDp)

    return Pair(dx, dy)
}

/**
 * Modifier that imperceptibly shifts content by -amplitudeDp to +amplitudeDp
 * every [stepMillis] along a smooth Lissajous curve to prevent OLED burn-in.
 *
 * Energy & Battery Architecture:
 * - Total step cycle: [stepMillis] (120,000ms / 2 minutes).
 * - Active transition: 2,000ms smooth linear interpolation to new subpixel offset.
 * - Static rest interval: 118,000ms (zero animation frames, zero recomposition, sleep-friendly).
 * - Clock source: [android.os.SystemClock.elapsedRealtime] (monotonic, immune to wall-clock/NTP shifts).
 *
 * @param enabled Whether pixel shifting is active.
 * @param stepMillis Total duration of one shift cycle in milliseconds (defaults to 120,000ms / 2 minutes).
 * @param amplitudeDp Maximum offset displacement in dp (e.g. 1.5f for widgets, 4.0f for high-contrast hero clock).
 */
fun Modifier.pixelShift(
    enabled: Boolean = true,
    stepMillis: Long = 120_000L,
    amplitudeDp: Float = 1.5f
): Modifier = if (!enabled) {
    this
} else {
    this.composed {
        var stepIndex by remember {
            mutableLongStateOf(android.os.SystemClock.elapsedRealtime() / stepMillis)
        }

        LaunchedEffect(stepMillis) {
            while (isActive) {
                delay(stepMillis)
                stepIndex = android.os.SystemClock.elapsedRealtime() / stepMillis
            }
        }

        val (targetDx, targetDy) = remember(stepIndex, amplitudeDp) {
            calculatePixelShiftOffset(stepIndex, amplitudeDp)
        }

        val animatedDx by animateFloatAsState(
            targetValue = targetDx,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
            label = "pixelShiftDx"
        )
        val animatedDy by animateFloatAsState(
            targetValue = targetDy,
            animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
            label = "pixelShiftDy"
        )

        this.graphicsLayer {
            translationX = animatedDx.dp.toPx()
            translationY = animatedDy.dp.toPx()
        }
    }
}

/**
 * Composable container that wraps [content] with OLED burn-in protection.
 */
@Composable
fun PixelShifter(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    stepMillis: Long = 120_000L,
    amplitudeDp: Float = 1.5f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.pixelShift(
            enabled = enabled,
            stepMillis = stepMillis,
            amplitudeDp = amplitudeDp
        )
    ) {
        content()
    }
}
