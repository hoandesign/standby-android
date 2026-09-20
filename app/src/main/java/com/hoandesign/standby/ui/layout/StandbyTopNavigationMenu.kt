package com.hoandesign.standby.ui.layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.hoandesign.standby.model.StandbyScreen
import com.hoandesign.standby.ui.theme.StandbyBorderSubtle
import com.hoandesign.standby.ui.theme.StandbyCardBg
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary

/**
 * Top floating navigation overlay for StandBy Android.
 *
 * In viewing mode:
 * - Sleek pill with STANDBY label, [ Bento | Clock | Music ] segmented tabs, [ Edit ] button, and [ Settings ] gear.
 * - Auto-hides after inactivity to let Bento boxes fill 100% fullscreen.
 *
 * In edit mode:
 * - Serves as the single unified top bar with EDITING status, [ Reset Defaults ], and [ Done ] pill.
 * - Eliminates dual-header visual clutter inside individual bento cards.
 */
@Composable
fun StandbyTopNavigationMenu(
    visible: Boolean,
    currentScreen: StandbyScreen,
    isEditMode: Boolean,
    accentColor: Color,
    onSelectScreen: (StandbyScreen) -> Unit,
    onToggleEditMode: () -> Unit,
    onOpenSettings: () -> Unit,
    onResetAllDefaults: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)),
        modifier = modifier.zIndex(20f)
    ) {
        if (isEditMode) {
            // Unified Edit Mode Bar (Replaces cluttered inner-card headers)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(StandbyCardBgSecondary.copy(alpha = 0.96f))
                    .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(22.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Editing Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(accentColor.copy(alpha = 0.18f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "EDITING BENTO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                            color = accentColor
                        )
                    }

                    // Center: Reset Defaults Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x22FFFFFF))
                            .clickable { onResetAllDefaults() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restore,
                                contentDescription = "Reset Defaults",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Reset Defaults",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextSecondary
                            )
                        }
                    }

                    // Right: Done Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(accentColor)
                            .clickable { onToggleEditMode() }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Done",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        } else {
            // Viewing Mode Floating Navigation Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(22.dp))
                    .background(StandbyCardBgSecondary.copy(alpha = 0.94f))
                    .border(1.dp, StandbyBorderSubtle, RoundedCornerShape(22.dp))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left: Micro STANDBY brand label
                    Text(
                        text = "STANDBY",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.8.sp,
                        color = TextTertiary
                    )

                    // Center: Segmented Navigation Tabs ([ Bento ] [ Clock ] [ Music ])
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0x22FFFFFF))
                            .padding(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StandbyScreen.entries.forEach { screen ->
                            val isSelected = currentScreen == screen
                            val tabTitle = when (screen) {
                                StandbyScreen.DUAL_WIDGET -> "Bento"
                                StandbyScreen.HERO_CLOCK -> "Clock"
                                StandbyScreen.NOW_PLAYING -> "Music"
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) StandbyCardBg.copy(alpha = 0.95f) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 0.5.dp else 0.dp,
                                        color = if (isSelected) StandbyBorderSubtle else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectScreen(screen) }
                                    .padding(horizontal = 9.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = tabTitle,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                            }
                        }
                    }

                    // Right: Contextual Controls (Edit button when on Bento page, Settings gear)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (currentScreen == StandbyScreen.DUAL_WIDGET) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x22FFFFFF))
                                    .clickable { onToggleEditMode() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Tune,
                                        contentDescription = "Customize",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }

                        // Settings Icon
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0x22FFFFFF))
                                .clickable { onOpenSettings() }
                                .padding(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = TextSecondary,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
