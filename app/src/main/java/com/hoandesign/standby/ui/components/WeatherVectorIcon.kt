package com.hoandesign.standby.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Grain
import androidx.compose.material.icons.rounded.Thunderstorm
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.WbCloudy
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hoandesign.standby.ui.theme.AccentAmber
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.NightRed

/**
 * Official Material Design Vector Weather Iconography.
 * Renders crisp, scalable vector graphics from Google's Material Design Icon pack.
 * Completely eliminates raw OS emojis with consistent, modern visual styling.
 */
@Composable
fun WeatherVectorIcon(
    condition: String,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    tint: Color = Color.White,
    isNightMode: Boolean = false
) {
    val activeTint = if (isNightMode) NightRed else tint
    val sunTint = if (isNightMode) NightRed else AccentAmber
    val rainTint = if (isNightMode) NightRed else AccentCyan

    val condLower = condition.lowercase()

    val (iconVector, iconTint) = when {
        condLower.contains("clear") || condLower.contains("sunny") -> {
            Pair(Icons.Rounded.WbSunny, sunTint)
        }
        condLower.contains("partly") || condLower.contains("mainly clear") -> {
            Pair(Icons.Rounded.WbCloudy, sunTint)
        }
        condLower.contains("rain") || condLower.contains("drizzle") || condLower.contains("shower") -> {
            Pair(Icons.Rounded.WaterDrop, rainTint)
        }
        condLower.contains("thunder") -> {
            Pair(Icons.Rounded.Thunderstorm, sunTint)
        }
        condLower.contains("snow") -> {
            Pair(Icons.Rounded.AcUnit, activeTint)
        }
        condLower.contains("fog") || condLower.contains("mist") -> {
            Pair(Icons.Rounded.Grain, activeTint)
        }
        else -> {
            Pair(Icons.Rounded.Cloud, activeTint)
        }
    }

    Icon(
        imageVector = iconVector,
        contentDescription = condition,
        tint = iconTint,
        modifier = modifier.size(size)
    )
}
