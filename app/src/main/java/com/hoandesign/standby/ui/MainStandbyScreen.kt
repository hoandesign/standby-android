package com.hoandesign.standby.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.NightModePreference
import com.hoandesign.standby.model.NightModeState
import com.hoandesign.standby.ui.components.NightModeFilterContainer
import com.hoandesign.standby.ui.components.pixelShift
import com.hoandesign.standby.ui.layout.AdaptiveStandbyLayout
import com.hoandesign.standby.ui.layout.MainStandbyPager
import com.hoandesign.standby.ui.theme.AccentAmber
import com.hoandesign.standby.ui.theme.AccentCyan
import com.hoandesign.standby.ui.theme.AccentGreen
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.AccentPurple
import com.hoandesign.standby.ui.theme.StandByTheme
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBg
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import com.hoandesign.standby.ui.widgets.battery.BatteryWidget
import com.hoandesign.standby.ui.widgets.calendar.AgendaWidget
import com.hoandesign.standby.ui.widgets.calendar.MonthCalendarWidget
import com.hoandesign.standby.ui.widgets.clock.AnalogClockWidget
import com.hoandesign.standby.ui.widgets.clock.BigDigitalClockWidget
import com.hoandesign.standby.ui.widgets.clock.RadialClockWidget
import com.hoandesign.standby.ui.widgets.clock.RetroFlipClockWidget
import com.hoandesign.standby.ui.widgets.clock.SolarArcClockWidget
import com.hoandesign.standby.ui.widgets.media.MusicPlayerWidget
import com.hoandesign.standby.ui.widgets.photo.PhotoFrameWidget
import com.hoandesign.standby.ui.widgets.system.SystemBentoWidget
import com.hoandesign.standby.ui.widgets.timer.DeskTimerWidget
import com.hoandesign.standby.ui.widgets.vibes.VibesWidget
import com.hoandesign.standby.ui.widgets.weather.WeatherWidget

/**
 * Curated accent colors available in StandBy settings:
 * Green, Orange, Amber, Cyan, Purple, White.
 */
val StandbyAccentColors = listOf(
    AccentGreen,
    AccentOrange,
    AccentAmber,
    AccentCyan,
    AccentPurple,
    Color.White
)

/**
 * Unified StandBy UI composing all widgets and features built in Tasks 1-7.
 *
 * Architecture:
 * - Monochromatic OLED Night Mode preservation via [NightModeFilterContainer].
 * - Continuous OLED burn-in protection via [Modifier.pixelShift].
 * - Dual-axis navigation via [MainStandbyPager] containing:
 *   - Left Slot Stack: AnalogClock, BigDigitalClock, RetroFlipClock, Weather, SolarArcClock, RadialClock.
 *   - Right Slot Stack: MonthCalendar, Agenda, Battery, MusicPlayer, SystemBento, DeskTimer, Vibes, PhotoFrame.
 *   - Hero Clock: RadialClock / BigDigitalClock full-screen (tap to switch).
 *   - Now Playing: MusicPlayer full-screen.
 * - Subtle glassmorphic quick settings sheet accessible via gear icon or bottom edge tap:
 *   - Night Mode toggle: Auto (< 5 lux), Always Red, Disabled.
 *   - Accent color selector: Green, Orange, Amber, Cyan, Purple, White.
 *   - Auto-launch on dock toggle.
 */
@Composable
fun MainStandbyScreen(
    modifier: Modifier = Modifier,
    nightModeState: NightModeState = remember { NightModeState() },
    onNightModeStateChange: ((NightModeState) -> Unit)? = null,
    accentColor: Color = AccentOrange,
    onAccentColorChange: ((Color) -> Unit)? = null,
    autoLaunchOnDock: Boolean = true,
    onAutoLaunchOnDockChange: ((Boolean) -> Unit)? = null
) {
    var currentNightModeState by remember(nightModeState) { mutableStateOf(nightModeState) }
    var currentAccentColor by remember(accentColor) { mutableStateOf(accentColor) }
    var currentAutoLaunch by remember(autoLaunchOnDock) { mutableStateOf(autoLaunchOnDock) }
    var showQuickSettings by remember { mutableStateOf(false) }

    // Left Slot Stack Widgets
    val leftSlotWidgets: List<@Composable () -> Unit> = remember(currentAccentColor) {
        listOf(
            { AnalogClockWidget() },
            { BigDigitalClockWidget(accentColor = currentAccentColor) },
            { RetroFlipClockWidget(accentColor = currentAccentColor) },
            { WeatherWidget() },
            { SolarArcClockWidget(accentColor = currentAccentColor) },
            { RadialClockWidget(accentColor = currentAccentColor) }
        )
    }

    // Right Slot Stack Widgets
    val rightSlotWidgets: List<@Composable () -> Unit> = remember(currentAccentColor) {
        listOf(
            { MonthCalendarWidget() },
            { AgendaWidget() },
            { BatteryWidget() },
            { MusicPlayerWidget(isCompact = true) },
            { SystemBentoWidget() },
            { DeskTimerWidget(accentColor = currentAccentColor) },
            { VibesWidget() },
            { PhotoFrameWidget() }
        )
    }

    StandByTheme(
        isNightMode = currentNightModeState.isNightModeActive,
        accentColor = currentAccentColor
    ) {
        NightModeFilterContainer(
            isNightMode = currentNightModeState.isNightModeActive,
            modifier = modifier
                .fillMaxSize()
                .pixelShift()
        ) {
            AdaptiveStandbyLayout(modifier = Modifier.fillMaxSize()) { archetype, _ ->
                Box(modifier = Modifier.fillMaxSize()) {
                    MainStandbyPager(
                        archetype = archetype,
                        leftSlotWidgets = leftSlotWidgets,
                        rightSlotWidgets = rightSlotWidgets,
                        heroClockContent = {
                            HeroClockView(accentColor = currentAccentColor)
                        },
                        nowPlayingContent = {
                            MusicPlayerWidget(
                                isCompact = false,
                                modifier = Modifier.fillMaxSize()
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Subtle top-end gear icon button for quick settings
                    IconButton(
                        onClick = { showQuickSettings = true },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 16.dp, end = 20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Quick Settings",
                            tint = TextSecondary.copy(alpha = 0.45f),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Bottom edge tap gesture zone
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(0.3f)
                            .height(28.dp)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                showQuickSettings = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .background(
                                    color = TextTertiary.copy(alpha = 0.35f),
                                    shape = CircleShape
                                )
                        )
                    }

                    // Glassmorphic Quick Settings Modal
                    QuickSettingsModal(
                        visible = showQuickSettings,
                        nightModeState = currentNightModeState,
                        onNightModePreferenceChange = { pref ->
                            val updated = currentNightModeState.withPreference(pref)
                            currentNightModeState = updated
                            onNightModeStateChange?.invoke(updated)
                        },
                        accentColor = currentAccentColor,
                        onAccentColorSelect = { color ->
                            currentAccentColor = color
                            onAccentColorChange?.invoke(color)
                        },
                        autoLaunchOnDock = currentAutoLaunch,
                        onAutoLaunchChange = { enabled ->
                            currentAutoLaunch = enabled
                            onAutoLaunchOnDockChange?.invoke(enabled)
                        },
                        onDismiss = { showQuickSettings = false }
                    )
                }
            }
        }
    }
}

/**
 * Full-screen Hero Clock view toggling between [RadialClockWidget] and [BigDigitalClockWidget] on tap.
 */
@Composable
private fun HeroClockView(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    var heroClockStyle by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                heroClockStyle = (heroClockStyle + 1) % 2
            },
        contentAlignment = Alignment.Center
    ) {
        if (heroClockStyle == 0) {
            RadialClockWidget(
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            BigDigitalClockWidget(
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * Subtle glassmorphic quick settings modal dialog.
 */
@Composable
private fun QuickSettingsModal(
    visible: Boolean,
    nightModeState: NightModeState,
    onNightModePreferenceChange: (NightModePreference) -> Unit,
    accentColor: Color,
    onAccentColorSelect: (Color) -> Unit,
    autoLaunchOnDock: Boolean,
    onAutoLaunchChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 3 },
        exit = fadeOut() + slideOutVertically { it / 3 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(min = 320.dp, max = 460.dp)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { /* Consume clicks inside sheet */ },
                shape = RoundedCornerShape(24.dp),
                color = StandbyCardBgSecondary.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, StandbyBorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "StandBy Settings",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StandbyBorder)
                    )

                    // Night Mode Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NIGHT MODE",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${nightModeState.ambientLux.toInt()} lux",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextTertiary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val options = listOf(
                                Triple(NightModePreference.AUTO, "Auto (< 5 lux)", "Auto (< 5 lux)"),
                                Triple(NightModePreference.ALWAYS_ON, "Always Red", "Always Red"),
                                Triple(NightModePreference.DISABLED, "Disabled", "Disabled")
                            )

                            options.forEach { (pref, _, label) ->
                                val isSelected = nightModeState.preference == pref
                                val optionBg = if (isSelected) accentColor.copy(alpha = 0.2f) else StandbyCardBg
                                val optionBorder = if (isSelected) accentColor else StandbyBorder
                                val optionTextColor = if (isSelected) accentColor else TextSecondary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .background(optionBg, RoundedCornerShape(12.dp))
                                        .border(1.dp, optionBorder, RoundedCornerShape(12.dp))
                                        .clickable { onNightModePreferenceChange(pref) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = optionTextColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StandbyBorder)
                    )

                    // Accent Color Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "ACCENT COLOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StandbyAccentColors.forEach { color ->
                                val isSelected = accentColor == color
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(color, CircleShape)
                                        .border(
                                            width = if (isSelected) 3.dp else 1.dp,
                                            color = if (isSelected) Color.White else Color(0x44FFFFFF),
                                            shape = CircleShape
                                        )
                                        .clickable { onAccentColorSelect(color) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = if (color == Color.White) Color.Black else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StandbyBorder)
                    )

                    // Auto-Launch on Dock Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto-Launch on Dock",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Launch StandBy when charging in landscape",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Switch(
                            checked = autoLaunchOnDock,
                            onCheckedChange = onAutoLaunchChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = accentColor,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = StandbyCardBg
                            )
                        )
                    }
                }
            }
        }
    }
}
