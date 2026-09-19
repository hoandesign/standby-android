package com.hoandesign.standby.ui.widgets.clock

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.ClockTime
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Vintage Split-Flap Flip Clock Widget.
 *
 * Features:
 * - Authentic 3D mechanical flip clock cards with upper and lower split flaps.
 * - Horizontal hairline divider slit and side axle hinge pins.
 * - Animated 3D folding flip effect when minute and hour digits transition.
 * - Upper and lower shadow gradients simulating overhead lighting and physical depth.
 * - AM/PM indicator badge tag.
 * - Seamless OLED deep red Night Mode adaptation.
 *
 * @param modifier Root modifier.
 * @param clockTime Optional static or externally provided time snapshot.
 * @param is24Hour If true, displays 24-hour time format (00..23).
 * @param accentColor Primary digit text color; defaults to [TextPrimary] (or [NightRed] in night mode).
 */
@Composable
fun RetroFlipClockWidget(
    modifier: Modifier = Modifier,
    clockTime: ClockTime? = null,
    is24Hour: Boolean = false,
    accentColor: Color = TextPrimary
) {
    val isNightMode = StandbyTheme.isNightMode
    val activeDigitColor = if (isNightMode) NightRed else accentColor

    // Live clock update loop (1-second check for minute flip)
    val time by produceState(
        initialValue = clockTime ?: ClockTime.now(is24Hour = is24Hour),
        key1 = clockTime,
        key2 = is24Hour
    ) {
        if (clockTime != null) {
            value = clockTime
            return@produceState
        }
        while (isActive) {
            value = ClockTime.now(is24Hour = is24Hour)
            delay(1000L)
        }
    }

    val hoursStr = time.formattedHours(is24Hour = is24Hour)
    val minutesStr = time.formattedMinutes()
    val amPmStr = time.amPm()

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(OledBlack)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        // Calculate responsive dimensions
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        val cardWidth = (availableWidth * 0.44f).coerceAtMost(availableHeight * 0.90f)
        val cardHeight = (cardWidth * 1.15f).coerceAtMost(availableHeight * 0.92f)
        val fontSize = (cardHeight.value * 0.58f).sp

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Hours Flip Card
            FlipCardUnit(
                value = hoursStr,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                fontSize = fontSize,
                digitColor = activeDigitColor,
                isNightMode = isNightMode,
                tagText = if (!is24Hour) amPmStr else null
            )

            // Center Column Colon Separator
            FlipColonSeparator(
                height = cardHeight,
                dotColor = if (isNightMode) NightRedDim else StandbyBorder
            )

            // Minutes Flip Card
            FlipCardUnit(
                value = minutesStr,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                fontSize = fontSize,
                digitColor = activeDigitColor,
                isNightMode = isNightMode,
                tagText = null
            )
        }
    }
}

/**
 * Individual split-flap flip card displaying 2 digits with mechanical 3D flip animation.
 */
@Composable
private fun FlipCardUnit(
    value: String,
    cardWidth: Dp,
    cardHeight: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    digitColor: Color,
    isNightMode: Boolean,
    tagText: String?
) {
    var previousValue by remember { mutableStateOf(value) }
    var currentValue by remember { mutableStateOf(value) }
    val flipProgress = remember { Animatable(0f) }

    LaunchedEffect(value) {
        if (value != currentValue) {
            previousValue = currentValue
            currentValue = value
            flipProgress.snapTo(0f)
            flipProgress.animateTo(
                targetValue = 180f,
                animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
            )
            previousValue = currentValue
        }
    }

    val halfHeight = cardHeight / 2f
    val cornerRadius = 14.dp
    val cardBg = Color(0xFF161618)
    val hingeColor = if (isNightMode) NightRedDim else Color(0xFF38383C)
    val dividerColor = Color(0xFF0A0A0B)

    Box(
        modifier = Modifier
            .size(width = cardWidth, height = cardHeight)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(cornerRadius))
            .border(
                width = 1.dp,
                color = if (isNightMode) NightRedDim else StandbyBorder,
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        // Base Card Stack
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Upper Half: Shows next/current incoming value
            HalfCard(
                text = currentValue,
                isTop = true,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                halfHeight = halfHeight,
                cornerRadius = cornerRadius,
                cardBg = cardBg,
                fontSize = fontSize,
                digitColor = digitColor
            )

            // Center Horizontal Slit Line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(dividerColor)
            )

            // Lower Half: Shows previous value until flip passes 90 deg
            HalfCard(
                text = if (flipProgress.value >= 90f) currentValue else previousValue,
                isTop = false,
                cardWidth = cardWidth,
                cardHeight = cardHeight,
                halfHeight = halfHeight,
                cornerRadius = cornerRadius,
                cardBg = cardBg,
                fontSize = fontSize,
                digitColor = digitColor
            )
        }

        // 3D Animated Flap Layer
        if (flipProgress.value in 1f..179f) {
            val angle = flipProgress.value
            if (angle < 90f) {
                // Top half folds down toward viewer
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(width = cardWidth, height = halfHeight)
                        .graphicsLayer {
                            rotationX = -angle
                            cameraDistance = 16f * density
                            transformOrigin = TransformOrigin(0.5f, 1.0f)
                        }
                ) {
                    HalfCard(
                        text = previousValue,
                        isTop = true,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        halfHeight = halfHeight,
                        cornerRadius = cornerRadius,
                        cardBg = cardBg,
                        fontSize = fontSize,
                        digitColor = digitColor
                    )
                    // Darken flap as it rotates away from light
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = (angle / 90f) * 0.4f))
                    )
                }
            } else {
                // Bottom half completes rotation downward
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(width = cardWidth, height = halfHeight)
                        .graphicsLayer {
                            rotationX = 180f - angle
                            cameraDistance = 16f * density
                            transformOrigin = TransformOrigin(0.5f, 0.0f)
                        }
                ) {
                    HalfCard(
                        text = currentValue,
                        isTop = false,
                        cardWidth = cardWidth,
                        cardHeight = cardHeight,
                        halfHeight = halfHeight,
                        cornerRadius = cornerRadius,
                        cardBg = cardBg,
                        fontSize = fontSize,
                        digitColor = digitColor
                    )
                    // Lighten flap as it approaches resting position
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = ((180f - angle) / 90f) * 0.4f))
                    )
                }
            }
        }

        // Left Hinge Notch
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(width = 4.dp, height = 12.dp)
                .background(hingeColor, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
        )

        // Right Hinge Notch
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(width = 4.dp, height = 12.dp)
                .background(hingeColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
        )

        // AM/PM Vintage Pill Tag (optional)
        if (tagText != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = tagText,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = if (isNightMode) NightRedDim else TextSecondary,
                        letterSpacing = 0.05.em
                    )
                )
            }
        }
    }
}

/**
 * Renders either the top half or bottom half of a flip card.
 */
@Composable
private fun HalfCard(
    text: String,
    isTop: Boolean,
    cardWidth: Dp,
    cardHeight: Dp,
    halfHeight: Dp,
    cornerRadius: Dp,
    cardBg: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    digitColor: Color
) {
    val shape = if (isTop) {
        RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
    } else {
        RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
    }

    val gradient = if (isTop) {
        Brush.verticalGradient(
            colors = listOf(
                cardBg.copy(alpha = 1.0f),
                Color(0xFF0E0E10)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF101012),
                cardBg.copy(alpha = 0.95f)
            )
        )
    }

    Box(
        modifier = Modifier
            .size(width = cardWidth, height = halfHeight)
            .clip(shape)
            .background(gradient)
            .clipToBounds()
    ) {
        // Position full-height text container offset so only half is visible
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(cardHeight)
                .offset(y = if (isTop) 0.dp else -halfHeight),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Black,
                    fontSize = fontSize,
                    lineHeight = fontSize,
                    letterSpacing = (-0.04).em,
                    color = digitColor,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}

/**
 * Vertical dual-dot separator between Hours and Minutes cards.
 */
@Composable
private fun FlipColonSeparator(
    height: Dp,
    dotColor: Color
) {
    Column(
        modifier = Modifier
            .height(height)
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(dotColor, CircleShape)
        )
    }
}
