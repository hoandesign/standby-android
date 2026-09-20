package com.hoandesign.standby.ui

import android.Manifest
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.TemperatureUnit
import com.hoandesign.standby.model.NightModePreference
import com.hoandesign.standby.model.NightModeState
import com.hoandesign.standby.model.StandbyWidgetId
import com.hoandesign.standby.model.StandbyWidgetRegistry
import com.hoandesign.standby.receiver.ChargingReceiver
import com.hoandesign.standby.util.PermissionHelper
import com.hoandesign.standby.ui.components.NightModeFilterContainer
import com.hoandesign.standby.ui.components.WidgetPickerSheet
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
import androidx.compose.runtime.DisposableEffect
import android.content.SharedPreferences
import com.hoandesign.standby.ui.widgets.clock.AnalogClockWidget
import com.hoandesign.standby.ui.widgets.clock.BigDigitalClockWidget
import com.hoandesign.standby.ui.widgets.clock.RadialClockWidget
import com.hoandesign.standby.ui.widgets.clock.RectangleAnalogClockWidget
import com.hoandesign.standby.ui.widgets.clock.RetroFlipClockWidget
import com.hoandesign.standby.ui.widgets.clock.SolarArcClockWidget
import com.hoandesign.standby.ui.widgets.media.MusicPlayerWidget

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
 * Unified StandBy UI composing all widgets and features.
 *
 * Architecture:
 * - Dynamic Bento Slot customization:
 *   - Left and Right bento slots can have widgets added, removed, and rearranged.
 *   - All 14 modular widgets can be picked from the Apple StandBy-style widget catalog.
 *   - Changes are saved persistently across app launches.
 * - True Full-Screen Single Slot Mode:
 *   - Any slot can expand into an immersive, edge-to-edge fullscreen widget display.
 *   - Smooth collapse back to dual bento layout.
 * - Monochromatic OLED Night Mode preservation via [NightModeFilterContainer].
 * - Continuous OLED burn-in protection via [Modifier.pixelShift].
 * - Dual-axis navigation via [MainStandbyPager].
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
    val context = LocalContext.current
    val prefs = remember(context) {
        context.getSharedPreferences(ChargingReceiver.PREFS_NAME, Context.MODE_PRIVATE)
    }

    var currentNightModeState by remember(nightModeState) { mutableStateOf(nightModeState) }
    var currentAccentColor by remember(accentColor) { mutableStateOf(accentColor) }
    var currentAutoLaunch by remember(autoLaunchOnDock) { mutableStateOf(autoLaunchOnDock) }
    var currentTemperatureUnit by remember {
        val raw = prefs.getString("pref_temp_unit", "C")
        mutableStateOf(if (raw == "F") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS)
    }
    var showQuickSettings by remember { mutableStateOf(false) }

    fun persistTemperatureUnit(newUnit: TemperatureUnit) {
        currentTemperatureUnit = newUnit
        prefs.edit().putString("pref_temp_unit", if (newUnit == TemperatureUnit.FAHRENHEIT) "F" else "C").apply()
    }

    DisposableEffect(prefs) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
            if (key == "pref_temp_unit") {
                val raw = sp.getString("pref_temp_unit", "C")
                currentTemperatureUnit = if (raw == "F") TemperatureUnit.FAHRENHEIT else TemperatureUnit.CELSIUS
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    // Dynamic Bento Slot State (persisted to SharedPreferences)
    var leftSlotWidgetIds by remember {
        val raw = prefs.getString(ChargingReceiver.KEY_LEFT_SLOT_WIDGETS, null)
        mutableStateOf(StandbyWidgetRegistry.deserializeWidgetList(raw, StandbyWidgetRegistry.defaultLeftSlot))
    }

    var rightSlotWidgetIds by remember {
        val raw = prefs.getString(ChargingReceiver.KEY_RIGHT_SLOT_WIDGETS, null)
        mutableStateOf(StandbyWidgetRegistry.deserializeWidgetList(raw, StandbyWidgetRegistry.defaultRightSlot))
    }

    var isEditMode by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var widgetPickerSlot by remember { mutableIntStateOf(0) }

    fun persistLeftSlot(newList: List<StandbyWidgetId>) {
        leftSlotWidgetIds = newList
        prefs.edit().putString(
            ChargingReceiver.KEY_LEFT_SLOT_WIDGETS,
            StandbyWidgetRegistry.serializeWidgetList(newList)
        ).apply()
    }

    fun persistRightSlot(newList: List<StandbyWidgetId>) {
        rightSlotWidgetIds = newList
        prefs.edit().putString(
            ChargingReceiver.KEY_RIGHT_SLOT_WIDGETS,
            StandbyWidgetRegistry.serializeWidgetList(newList)
        ).apply()
    }

    fun onAddWidget(slotIndex: Int, widgetId: StandbyWidgetId) {
        if (slotIndex == 0) {
            val updated = leftSlotWidgetIds + widgetId
            persistLeftSlot(updated)
        } else {
            val updated = rightSlotWidgetIds + widgetId
            persistRightSlot(updated)
        }
    }

    fun onRemoveWidget(slotIndex: Int, widgetIndex: Int) {
        if (slotIndex == 0 && leftSlotWidgetIds.size > 1 && widgetIndex in leftSlotWidgetIds.indices) {
            val updated = leftSlotWidgetIds.toMutableList().apply { removeAt(widgetIndex) }
            persistLeftSlot(updated)
        } else if (slotIndex == 1 && rightSlotWidgetIds.size > 1 && widgetIndex in rightSlotWidgetIds.indices) {
            val updated = rightSlotWidgetIds.toMutableList().apply { removeAt(widgetIndex) }
            persistRightSlot(updated)
        }
    }

    fun onReorderWidgets(slotIndex: Int, from: Int, to: Int) {
        if (slotIndex == 0) {
            if (from in leftSlotWidgetIds.indices && to in leftSlotWidgetIds.indices) {
                val updated = leftSlotWidgetIds.toMutableList()
                val item = updated.removeAt(from)
                updated.add(to, item)
                persistLeftSlot(updated)
            }
        } else {
            if (from in rightSlotWidgetIds.indices && to in rightSlotWidgetIds.indices) {
                val updated = rightSlotWidgetIds.toMutableList()
                val item = updated.removeAt(from)
                updated.add(to, item)
                persistRightSlot(updated)
            }
        }
    }

    fun onResetDefaults() {
        persistLeftSlot(StandbyWidgetRegistry.defaultLeftSlot)
        persistRightSlot(StandbyWidgetRegistry.defaultRightSlot)
    }

    StandByTheme(
        isNightMode = currentNightModeState.isNightModeActive,
        accentColor = currentAccentColor,
        temperatureUnit = currentTemperatureUnit
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
                        leftSlotWidgetIds = leftSlotWidgetIds,
                        rightSlotWidgetIds = rightSlotWidgetIds,
                        accentColor = currentAccentColor,
                        isEditMode = isEditMode,
                        onToggleEditMode = { isEditMode = !isEditMode },
                        onOpenSettings = { showQuickSettings = true },
                        onOpenWidgetPicker = { slotIdx ->
                            widgetPickerSlot = slotIdx
                            showWidgetPicker = true
                        },
                        onRemoveWidgetFromSlot = { slotIdx, widgetIdx ->
                            onRemoveWidget(slotIdx, widgetIdx)
                        },
                        onReorderSlotWidgets = { slotIdx, from, to ->
                            onReorderWidgets(slotIdx, from, to)
                        },
                        onResetSlotDefaults = { slotIdx ->
                            if (slotIdx == 0) persistLeftSlot(StandbyWidgetRegistry.defaultLeftSlot)
                            else persistRightSlot(StandbyWidgetRegistry.defaultRightSlot)
                        },
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

                    // Widget Picker Sheet
                    WidgetPickerSheet(
                        visible = showWidgetPicker,
                        slotIndex = widgetPickerSlot,
                        currentSlotWidgets = if (widgetPickerSlot == 0) leftSlotWidgetIds else rightSlotWidgetIds,
                        accentColor = currentAccentColor,
                        onSelectWidget = { widgetId ->
                            onAddWidget(widgetPickerSlot, widgetId)
                        },
                        onResetDefaults = { onResetDefaults() },
                        onDismiss = { showWidgetPicker = false }
                    )

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
                        temperatureUnit = currentTemperatureUnit,
                        onTemperatureUnitChange = { unit ->
                            persistTemperatureUnit(unit)
                        },
                        autoLaunchOnDock = currentAutoLaunch,
                        onAutoLaunchChange = { enabled ->
                            currentAutoLaunch = enabled
                            onAutoLaunchOnDockChange?.invoke(enabled)
                        },
                        onCustomizeSlots = {
                            isEditMode = true
                        },
                        onDismiss = { showQuickSettings = false }
                    )
                }
            }
        }
    }
}

/**
 * Full-screen Hero Clock suite cycling between 6 clock faces on tap:
 * 0: [RectangleAnalogClockWidget] (Tank Bauhaus dial)
 * 1: [RadialClockWidget] (Cardinal typography dial)
 * 2: [BigDigitalClockWidget] (iOS 18 digital typography)
 * 3: [AnalogClockWidget] (Classic Swiss dial)
 * 4: [RetroFlipClockWidget] (Split-flap mechanical flip)
 * 5: [SolarArcClockWidget] (Sun trajectory arc)
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
                heroClockStyle = (heroClockStyle + 1) % 6
            },
        contentAlignment = Alignment.Center
    ) {
        when (heroClockStyle) {
            0 -> RectangleAnalogClockWidget(
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
            1 -> RadialClockWidget(
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
            2 -> BigDigitalClockWidget(
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
            3 -> AnalogClockWidget(
                modifier = Modifier.fillMaxSize()
            )
            4 -> RetroFlipClockWidget(
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
            else -> SolarArcClockWidget(
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
    temperatureUnit: TemperatureUnit,
    onTemperatureUnitChange: (TemperatureUnit) -> Unit,
    autoLaunchOnDock: Boolean,
    onAutoLaunchChange: (Boolean) -> Unit,
    onCustomizeSlots: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var hasLocation by remember { mutableStateOf(PermissionHelper.isLocationGranted(context)) }
    var hasCalendar by remember { mutableStateOf(PermissionHelper.isCalendarGranted(context)) }
    var hasMedia by remember { mutableStateOf(PermissionHelper.isNotificationListenerGranted(context)) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        hasLocation = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val calendarLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCalendar = granted
    }

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

                    // Customize Bento Slots Action Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(StandbyCardBg)
                            .border(1.dp, StandbyBorder, RoundedCornerShape(14.dp))
                            .clickable {
                                onDismiss()
                                onCustomizeSlots()
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(accentColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Customize Stacks",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Customize Bento Slots",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Add, remove & rearrange widgets",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Open",
                            tint = TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Divider
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(StandbyBorder)
                    )

                    // Permissions & Real Data Sync Section
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "PERMISSIONS & REAL DATA",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        // Location & Live Weather
                        DataSyncRow(
                            iconVector = Icons.Rounded.LocationOn,
                            title = "Live Weather & GPS",
                            status = if (hasLocation) "Connected" else "Connect",
                            isGranted = hasLocation,
                            accentColor = accentColor,
                            onClick = {
                                if (!hasLocation) {
                                    locationLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                } else {
                                    PermissionHelper.openAppSettings(context)
                                }
                            }
                        )

                        // Calendar & Agenda
                        DataSyncRow(
                            iconVector = Icons.Rounded.CalendarToday,
                            title = "Calendar Agenda",
                            status = if (hasCalendar) "Connected" else "Connect",
                            isGranted = hasCalendar,
                            accentColor = accentColor,
                            onClick = {
                                if (!hasCalendar) {
                                    calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                                } else {
                                    PermissionHelper.openAppSettings(context)
                                }
                            }
                        )

                        // Media Session Sync
                        DataSyncRow(
                            iconVector = Icons.Rounded.MusicNote,
                            title = "Spotify & Media Sync",
                            status = if (hasMedia) "Connected" else "Connect",
                            isGranted = hasMedia,
                            accentColor = accentColor,
                            onClick = {
                                PermissionHelper.openNotificationListenerSettings(context)
                            }
                        )
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
                                Triple(NightModePreference.DISABLED, "Disabled", "Disabled"),
                                Triple(NightModePreference.AUTO, "Auto (< 5 lux)", "Auto (< 5 lux)"),
                                Triple(NightModePreference.ALWAYS_ON, "Always Red", "Always Red")
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

                    // Temperature Unit Section (°C vs °F)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "TEMPERATURE UNIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val units = listOf(
                                Pair(TemperatureUnit.CELSIUS, "Celsius (°C)"),
                                Pair(TemperatureUnit.FAHRENHEIT, "Fahrenheit (°F)")
                            )

                            units.forEach { (unit, label) ->
                                val isSelected = temperatureUnit == unit
                                val optionBg = if (isSelected) accentColor.copy(alpha = 0.2f) else StandbyCardBg
                                val optionBorder = if (isSelected) accentColor else StandbyBorder
                                val optionTextColor = if (isSelected) accentColor else TextSecondary

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .background(optionBg, RoundedCornerShape(12.dp))
                                        .border(1.dp, optionBorder, RoundedCornerShape(12.dp))
                                        .clickable { onTemperatureUnitChange(unit) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        color = optionTextColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

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

/**
 * Clean glassmorphic status row for managing real data and runtime permissions.
 */
@Composable
private fun DataSyncRow(
    iconVector: ImageVector,
    title: String,
    status: String,
    isGranted: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(StandbyCardBg)
            .border(1.dp, if (isGranted) StandbyBorder else accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = title,
                tint = if (isGranted) accentColor else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isGranted) Color(0x2230D158) else accentColor.copy(alpha = 0.18f))
                .border(
                    1.dp,
                    if (isGranted) Color(0xFF30D158) else accentColor,
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = status,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isGranted) Color(0xFF30D158) else accentColor
            )
        }
    }
}
