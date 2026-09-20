package com.hoandesign.standby.ui.widgets.photo

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.hoandesign.standby.ui.theme.NightRed
import com.hoandesign.standby.ui.theme.NightRedDim
import com.hoandesign.standby.ui.theme.OledBlack
import com.hoandesign.standby.ui.theme.StandbyBorder
import com.hoandesign.standby.ui.theme.StandbyCardBg
import com.hoandesign.standby.ui.theme.StandbyCardBgSecondary
import com.hoandesign.standby.ui.theme.StandbyTheme
import com.hoandesign.standby.ui.theme.TextPrimary
import com.hoandesign.standby.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Curated ambient nature and landscape scenes.
 */
enum class AmbientScene(val sceneName: String) {
    MOUNTAIN_DUSK("Mountain Dusk"),
    MIDNIGHT_AURORA("Midnight Aurora"),
    COASTAL_SUNSET("Coastal Sunset"),
    MISTY_FOREST("Misty Forest")
}

/**
 * Ambient Photo Frame Widget providing an artistic digital picture frame experience
 * with slow Ken Burns pan/zoom animation, real gallery photo picking, and elegant date/time overlay.
 *
 * Features:
 * - Real user gallery photo integration via standard Android Photo Picker.
 * - Subtle Ken Burns slow camera drift (scale 1.0f to 1.12f, continuous translation).
 * - Curated artistic atmospheric landscape scenes when no custom photo is chosen.
 * - Elegant glassmorphic date/time badge with live second updates.
 * - Deep crimson OLED preservation in Night Mode.
 * - Tap to cycle scenes, button to pick local pictures.
 *
 * @param modifier Root modifier.
 * @param initialScene Initial landscape scene.
 */
@Composable
fun PhotoFrameWidget(
    modifier: Modifier = Modifier,
    initialScene: AmbientScene = AmbientScene.MOUNTAIN_DUSK
) {
    val context = LocalContext.current
    var currentScene by remember { mutableIntStateOf(initialScene.ordinal) }
    val scenes = AmbientScene.entries
    val isNightMode = StandbyTheme.isNightMode

    var userCustomBitmap by remember { mutableStateOf<ImageBitmap?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val bmp = BitmapFactory.decodeStream(stream)
                    if (bmp != null) {
                        userCustomBitmap = bmp.asImageBitmap()
                    }
                }
            } catch (_: Exception) {
                // Ignore load error
            }
        }
    }

    // Real-time date & time ticking
    val currentDateTime by produceState(initialValue = ZonedDateTime.now()) {
        while (isActive) {
            delay(1000L)
            value = ZonedDateTime.now()
        }
    }

    // Ken Burns slow scale animation
    val infiniteTransition = rememberInfiniteTransition(label = "KenBurnsTransition")
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(14000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsScale"
    )

    // Ken Burns slow pan translation
    val panXAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanX"
    )

    val panYAnim by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(22000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "KenBurnsPanY"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(20.dp))
            .background(OledBlack)
            .clickable {
                // Tap advances to next photo scene or cycles back from custom photo
                if (userCustomBitmap != null) {
                    userCustomBitmap = null
                } else {
                    currentScene = (currentScene + 1) % scenes.size
                }
            }
    ) {
        // Ken Burns animated photo container
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scaleAnim)
                .offset { IntOffset(panXAnim.roundToInt(), panYAnim.roundToInt()) }
        ) {
            val custom = userCustomBitmap
            if (custom != null) {
                Image(
                    bitmap = custom,
                    contentDescription = "User Photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Crossfade(
                    targetState = scenes[currentScene],
                    animationSpec = tween(1000),
                    label = "SceneCrossfade"
                ) { scene ->
                    SceneCanvas(
                        scene = scene,
                        isNightMode = isNightMode,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Elegant date/time glassmorphic overlay anchored in bottom-left
        val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.US) }
        val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.US) }

        val overlayBg = if (isNightMode) Color(0x33FF453A) else StandbyCardBg.copy(alpha = 0.78f)
        val overlayBorder = if (isNightMode) NightRedDim else StandbyBorder

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(overlayBg)
                .border(1.dp, overlayBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Column {
                Text(
                    text = currentDateTime.format(timeFormatter),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = (-0.02).em,
                        color = if (isNightMode) NightRed else TextPrimary
                    )
                )
                Text(
                    text = currentDateTime.format(dateFormatter),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.Medium,
                        fontSize = 11.sp,
                        color = if (isNightMode) Color(0xAAFF453A) else TextSecondary
                    )
                )
            }
        }

        // Top-right action controls: Scene Name Pill & Photo Picker Button
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pick photo button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(StandbyCardBgSecondary.copy(alpha = 0.85f))
                    .border(1.dp, StandbyBorder, RoundedCornerShape(10.dp))
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (userCustomBitmap != null) "🖼️ Custom" else "📷 Pick Photo",
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = if (isNightMode) NightRed else Color.White
                    )
                )
            }

            if (userCustomBitmap == null) {
                Text(
                    text = scenes[currentScene].sceneName.uppercase(),
                    style = TextStyle(
                        fontFamily = FontFamily.Default,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 9.sp,
                        letterSpacing = 0.08.em,
                        color = if (isNightMode) Color(0x88FF453A) else Color(0x88FFFFFF)
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/**
 * Procedural rendering of atmospheric landscape scenes.
 */
@Composable
private fun SceneCanvas(
    scene: AmbientScene,
    isNightMode: Boolean,
    modifier: Modifier = Modifier
) {
    if (isNightMode) {
        // Deep monochromatic red starscape
        Canvas(modifier = modifier) {
            drawRect(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF140202), Color(0xFF330505), Color(0xFF0A0000))
                )
            )
            // Night star points
            val starPoints = listOf(
                Offset(size.width * 0.15f, size.height * 0.2f),
                Offset(size.width * 0.45f, size.height * 0.12f),
                Offset(size.width * 0.75f, size.height * 0.25f),
                Offset(size.width * 0.88f, size.height * 0.35f)
            )
            for (p in starPoints) {
                drawCircle(color = Color(0x66FF453A), radius = 1.5.dp.toPx(), center = p)
            }
        }
        return
    }

    when (scene) {
        AmbientScene.MOUNTAIN_DUSK -> {
            Canvas(modifier = modifier) {
                // Sky gradient: deep violet to dusty rose
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF312E81), Color(0xFF6B21A8), Color(0xFFBE185D))
                    )
                )

                // Silhouette Mountain Ridge (Back)
                val backMountain = Path().apply {
                    moveTo(0f, size.height * 0.7f)
                    lineTo(size.width * 0.35f, size.height * 0.45f)
                    lineTo(size.width * 0.65f, size.height * 0.62f)
                    lineTo(size.width, size.height * 0.42f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(backMountain, color = Color(0x881E1B4B))

                // Silhouette Mountain Ridge (Front)
                val frontMountain = Path().apply {
                    moveTo(0f, size.height * 0.82f)
                    lineTo(size.width * 0.22f, size.height * 0.60f)
                    lineTo(size.width * 0.52f, size.height * 0.75f)
                    lineTo(size.width * 0.82f, size.height * 0.54f)
                    lineTo(size.width * 0.72f, size.height * 0.72f)
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                drawPath(frontMountain, color = Color(0xFF09090B))
            }
        }
        AmbientScene.MIDNIGHT_AURORA -> {
            Canvas(modifier = modifier) {
                // Pitch night cosmic sky
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF020617), Color(0xFF030712), Color(0xFF064E3B))
                    )
                )
                // Luminous Aurora borealis waves
                val auroraPath = Path().apply {
                    moveTo(0f, size.height * 0.35f)
                    cubicTo(
                        size.width * 0.25f, size.height * 0.15f,
                        size.width * 0.65f, size.height * 0.5f,
                        size.width, size.height * 0.25f
                    )
                    lineTo(size.width, size.height * 0.6f)
                    cubicTo(
                        size.width * 0.65f, size.height * 0.75f,
                        size.width * 0.25f, size.height * 0.45f,
                        0f, size.height * 0.55f
                    )
                    close()
                }
                drawPath(
                    auroraPath,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0x55059669), Color(0xAA10B981), Color(0x6606B6D4))
                    )
                )
            }
        }
        AmbientScene.COASTAL_SUNSET -> {
            Canvas(modifier = modifier) {
                // Golden hour gradient
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1E1B4B), Color(0xFFB91C1C), Color(0xFFEA580C), Color(0xFFFBBF24))
                    )
                )
                // Ocean water reflecting horizon
                drawRect(
                    color = Color(0xDD0C0A09),
                    topLeft = Offset(0f, size.height * 0.68f),
                    size = androidx.compose.ui.geometry.Size(size.width, size.height * 0.32f)
                )
            }
        }
        AmbientScene.MISTY_FOREST -> {
            Canvas(modifier = modifier) {
                // Foggy forest sky
                drawRect(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155))
                    )
                )
                // Pine tree silhouettes
                val forestPath = Path().apply {
                    moveTo(0f, size.height)
                    lineTo(0f, size.height * 0.6f)
                    var curX = 0f
                    val step = size.width / 12f
                    while (curX <= size.width) {
                        lineTo(curX + step * 0.5f, size.height * 0.45f)
                        lineTo(curX + step, size.height * 0.65f)
                        curX += step
                    }
                    lineTo(size.width, size.height)
                    close()
                }
                drawPath(forestPath, color = Color(0xFF050505))
            }
        }
    }
}
