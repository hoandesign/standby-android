package com.hoandesign.standby.ui.widgets.media

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.hoandesign.standby.service.StandbyMediaListenerService
import com.hoandesign.standby.service.MediaPlaybackState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.model.DEFAULT_MEDIA_TRACK
import com.hoandesign.standby.model.MediaTrack
import com.hoandesign.standby.model.formatMediaDuration
import com.hoandesign.standby.ui.theme.AccentOrange
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBg
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import com.hoandesign.standby.ui.theme.TextTertiary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Now Playing Music Player widget supporting both compact widget card
 * and immersive fullscreen StandBy media display.
 *
 * Features:
 * - Album artwork card with continuously spinning vinyl record disc (`rotationZ`).
 * - Realistic concentric vinyl sound grooves and glossy sheen.
 * - Scrubbable media progress bar with live position & remaining time.
 * - Media controls: Previous, Play/Pause toggle, and Next.
 * - Built-in default track: "Midnight City" by M83.
 * - Monochromatic deep red adaptation in Night Mode.
 *
 * @param modifier Root modifier.
 * @param initialTrack Initial media track metadata.
 * @param isCompact If true, optimizes proportions for dual-stack widget card slots.
 */
@Composable
fun MusicPlayerWidget(
    modifier: Modifier = Modifier,
    initialTrack: MediaTrack = DEFAULT_MEDIA_TRACK,
    isCompact: Boolean? = null
) {
    var track by remember { mutableStateOf(initialTrack) }
    var isPlaying by remember { mutableStateOf(track.isPlaying) }
    var currentPositionMs by remember { mutableLongStateOf(track.positionMs) }

    val isNightMode = StandbyTheme.isNightMode
    val activeAccent = if (isNightMode) NightRed else StandbyTheme.accentColor

    // Live position ticking when actively playing
    LaunchedEffect(isPlaying) {
        while (isActive && isPlaying) {
            delay(1000L)
            if (currentPositionMs + 1000L >= track.durationMs) {
                currentPositionMs = 0L // Loop track
            } else {
                currentPositionMs += 1000L
            }
        }
    }

    // Spin animation that ONLY runs when isPlaying == true
    var effectiveAngle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastTime = androidx.compose.runtime.withFrameNanos { it }
            while (isActive) {
                val currentTime = androidx.compose.runtime.withFrameNanos { it }
                val deltaMs = (currentTime - lastTime) / 1_000_000f
                effectiveAngle = (effectiveAngle + (deltaMs / 6000f) * 360f) % 360f
                lastTime = currentTime
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
    ) {
        val compactMode = isCompact ?: (maxWidth < 420.dp)

        if (compactMode) {
            CompactMusicPlayerLayout(
                track = track,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                rotationAngle = effectiveAngle,
                isNightMode = isNightMode,
                activeAccent = activeAccent,
                onTogglePlay = { isPlaying = !isPlaying },
                onSeek = { newFraction ->
                    currentPositionMs = (newFraction * track.durationMs).toLong()
                },
                onPrev = { currentPositionMs = 0L },
                onNext = {
                    // Alternate track for demonstration
                    track = if (track.title == "Midnight City") {
                        MediaTrack(
                            title = "Resonance",
                            artist = "HOME",
                            album = "Odyssey",
                            durationMs = 212_000L,
                            positionMs = 32_000L,
                            isPlaying = true
                        )
                    } else {
                        DEFAULT_MEDIA_TRACK
                    }
                    currentPositionMs = track.positionMs
                    isPlaying = true
                }
            )
        } else {
            FullscreenMusicPlayerLayout(
                track = track,
                isPlaying = isPlaying,
                currentPositionMs = currentPositionMs,
                rotationAngle = effectiveAngle,
                isNightMode = isNightMode,
                activeAccent = activeAccent,
                onTogglePlay = { isPlaying = !isPlaying },
                onSeek = { newFraction ->
                    currentPositionMs = (newFraction * track.durationMs).toLong()
                },
                onPrev = { currentPositionMs = 0L },
                onNext = {
                    track = if (track.title == "Midnight City") {
                        MediaTrack(
                            title = "Resonance",
                            artist = "HOME",
                            album = "Odyssey",
                            durationMs = 212_000L,
                            positionMs = 32_000L,
                            isPlaying = true
                        )
                    } else {
                        DEFAULT_MEDIA_TRACK
                    }
                    currentPositionMs = track.positionMs
                    isPlaying = true
                }
            )
        }
    }
}

/**
 * Compact layout tailored for dual-stack widget cards.
 */
@Composable
private fun CompactMusicPlayerLayout(
    track: MediaTrack,
    isPlaying: Boolean,
    currentPositionMs: Long,
    rotationAngle: Float,
    isNightMode: Boolean,
    activeAccent: Color,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top section: Album artwork + Vinyl preview alongside track titles
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Album & Vinyl combo badge
            Box(
                modifier = Modifier
                    .size(64.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                // Vinyl disc slightly slipping behind album cover
                VinylRecordDisc(
                    modifier = Modifier
                        .size(54.dp)
                        .align(Alignment.CenterEnd)
                        .offset(x = 6.dp)
                        .rotate(rotationAngle),
                    isNightMode = isNightMode
                )

                // Album art card
                AlbumArtCover(
                    modifier = Modifier
                        .size(52.dp)
                        .align(Alignment.CenterStart),
                    isNightMode = isNightMode
                )
            }

            // Track metadata
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = track.title,
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (isNightMode) NightRed else TextPrimary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} · ${track.album}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Scrubbable Progress Bar
        val progressFraction = if (track.durationMs > 0) {
            (currentPositionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ScrubProgressBar(
                progressFraction = progressFraction,
                onSeek = onSeek,
                activeAccent = activeAccent,
                isNightMode = isNightMode,
                heightDp = 6
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMediaDuration(currentPositionMs),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 10.sp,
                        color = if (isNightMode) Color(0x88FF453A) else TextTertiary
                    )
                )

                val remainingMs = (track.durationMs - currentPositionMs).coerceAtLeast(0L)
                Text(
                    text = "-${formatMediaDuration(remainingMs)}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 10.sp,
                        color = if (isNightMode) Color(0x88FF453A) else TextTertiary
                    )
                )
            }
        }

        // Media Control Buttons (⏮, ⏯, ⏭)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous
            Icon(
                imageVector = Icons.Filled.SkipPrevious,
                contentDescription = "Previous Track",
                tint = if (isNightMode) Color(0xCCFF453A) else TextSecondary,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { 
                            StandbyMediaListenerService.instance?.skipToPrevious()
                            onPrev()
                        }
                    .padding(4.dp)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Play / Pause Toggle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isNightMode) NightRedDim else StandbyCardBgSecondary)
                    .border(
                        1.dp,
                        if (isNightMode) NightRed else StandbyBorder,
                        CircleShape
                    )
                    .clickable { onTogglePlay() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isNightMode) NightRed else TextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Next
            Icon(
                imageVector = Icons.Filled.SkipNext,
                contentDescription = "Next Track",
                tint = if (isNightMode) Color(0xCCFF453A) else TextSecondary,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .clickable { 
                            StandbyMediaListenerService.instance?.skipToNext()
                            onNext()
                        }
                    .padding(4.dp)
            )
        }
    }
}

/**
 * Immersive widescreen layout for StandbyScreen.NOW_PLAYING.
 */
@Composable
private fun FullscreenMusicPlayerLayout(
    track: MediaTrack,
    isPlaying: Boolean,
    currentPositionMs: Long,
    rotationAngle: Float,
    isNightMode: Boolean,
    activeAccent: Color,
    onTogglePlay: () -> Unit,
    onSeek: (Float) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(36.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left Column: Album Art + Spinning Vinyl Record
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                // Vinyl Disc slipping out of sleeve
                VinylRecordDisc(
                    modifier = Modifier
                        .size(190.dp)
                        .offset(x = 32.dp)
                        .rotate(rotationAngle),
                    isNightMode = isNightMode
                )

                // Album Art Front Cover
                AlbumArtCover(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(x = (-16).dp),
                    isNightMode = isNightMode
                )
            }
        }

        // Right Column: Track Info, Scrub Bar, Controls
        Column(
            modifier = Modifier
                .weight(1.2f)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center
        ) {
            // Track Title
            Text(
                text = track.title,
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    letterSpacing = (-0.02).em,
                    color = if (isNightMode) NightRed else TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Artist & Album
            Text(
                text = "${track.artist} — ${track.album}",
                style = TextStyle(
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Scrub Bar
            val progressFraction = if (track.durationMs > 0) {
                (currentPositionMs.toFloat() / track.durationMs.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            ScrubProgressBar(
                progressFraction = progressFraction,
                onSeek = onSeek,
                activeAccent = activeAccent,
                isNightMode = isNightMode,
                heightDp = 8
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatMediaDuration(currentPositionMs),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = if (isNightMode) Color(0x88FF453A) else TextTertiary
                    )
                )

                val remainingMs = (track.durationMs - currentPositionMs).coerceAtLeast(0L)
                Text(
                    text = "-${formatMediaDuration(remainingMs)}",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontSize = 12.sp,
                        color = if (isNightMode) Color(0x88FF453A) else TextTertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Media Controls (⏮, ⏯, ⏭)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipPrevious,
                    contentDescription = "Previous Track",
                    tint = if (isNightMode) Color(0xCCFF453A) else TextSecondary,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { 
                            StandbyMediaListenerService.instance?.skipToPrevious()
                            onPrev()
                        }
                        .padding(6.dp)
                )

                Spacer(modifier = Modifier.width(28.dp))

                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(if (isNightMode) NightRedDim else StandbyCardBgSecondary)
                        .border(
                            1.5.dp,
                            if (isNightMode) NightRed else StandbyBorder,
                            CircleShape
                        )
                        .clickable {
                            val service = StandbyMediaListenerService.instance
                            if (service != null) {
                                if (isPlaying) service.pause() else service.play()
                            }
                            onTogglePlay()
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isNightMode) NightRed else TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(28.dp))

                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next Track",
                    tint = if (isNightMode) Color(0xCCFF453A) else TextSecondary,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .clickable { 
                            StandbyMediaListenerService.instance?.skipToNext()
                            onNext()
                        }
                        .padding(6.dp)
                )
            }
        }
    }
}

/**
 * Stylized album artwork cover.
 */
@Composable
private fun AlbumArtCover(
    modifier: Modifier = Modifier,
    isNightMode: Boolean
) {
    val shape = RoundedCornerShape(12.dp)
    val gradientColors = if (isNightMode) {
        listOf(Color(0xFF330808), Color(0xFF661010), Color(0xFF991515))
    } else {
        listOf(Color(0xFF2E0854), Color(0xFF511882), Color(0xFFE94057), Color(0xFFF27121))
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(Brush.linearGradient(gradientColors))
            .border(
                1.dp,
                if (isNightMode) NightRedDim else StandbyBorder,
                shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // Minimalist vinyl sleeve label / icon
        Text(
            text = "♪",
            style = TextStyle(
                fontFamily = FontFamily.Default,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 0.1.em,
                color = if (isNightMode) Color(0xCCFF453A) else Color(0xDDFFFFFF)
            )
        )
    }
}

/**
 * Procedurally drawn vinyl record disc with concentric grooves and center hub label.
 */
@Composable
private fun VinylRecordDisc(
    modifier: Modifier = Modifier,
    isNightMode: Boolean
) {
    val discBaseColor = Color(0xFF111113)
    val grooveColor = if (isNightMode) Color(0x22FF453A) else Color(0x22FFFFFF)
    val labelColor = if (isNightMode) Color(0xFF4A1010) else AccentOrange

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val centerOffset = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        // Outer black vinyl body
        drawCircle(
            color = discBaseColor,
            radius = maxRadius,
            center = centerOffset
        )

        // Concentric vinyl audio grooves
        val grooveFractions = listOf(0.92f, 0.84f, 0.76f, 0.68f, 0.60f, 0.52f, 0.44f)
        for (fraction in grooveFractions) {
            drawCircle(
                color = grooveColor,
                radius = maxRadius * fraction,
                center = centerOffset,
                style = Stroke(width = 1.2.dp.toPx())
            )
        }

        // Center album sticker label
        drawCircle(
            color = labelColor,
            radius = maxRadius * 0.32f,
            center = centerOffset
        )

        // Center spindle hole
        drawCircle(
            color = OledBlack,
            radius = maxRadius * 0.08f,
            center = centerOffset
        )
    }
}

/**
 * Custom scrubbable horizontal progress bar supporting click & drag scrubbing.
 */
@Composable
private fun ScrubProgressBar(
    progressFraction: Float,
    onSeek: (Float) -> Unit,
    activeAccent: Color,
    isNightMode: Boolean,
    heightDp: Int
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height((heightDp + 10).dp)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val fraction = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onSeek(fraction)
                }
            }
            .clickable {
                // Allows tapping anywhere on track
            },
        contentAlignment = Alignment.CenterStart
    ) {
        val widthPx = constraints.maxWidth.toFloat()

        // Background track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heightDp.dp)
                .clip(RoundedCornerShape((heightDp / 2).dp))
                .background(if (isNightMode) Color(0x22FF453A) else StandbyCardBgSecondary)
                .border(
                    1.dp,
                    if (isNightMode) NightRedDim else StandbyBorder,
                    RoundedCornerShape((heightDp / 2).dp)
                )
        )

        // Filled active progress
        if (progressFraction > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction)
                    .height(heightDp.dp)
                    .clip(RoundedCornerShape((heightDp / 2).dp))
                    .background(activeAccent)
            )
        }
    }
}
