package com.hoandesign.standby.model

import java.util.Locale

/**
 * Immutable representation of a music or media playback track.
 * Defaults to idle state without mock or hardcoded titles.
 */
data class MediaTrack(
    val title: String = "Not Playing",
    val artist: String = "No active audio session",
    val album: String = "",
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false
) {
    /**
     * Fractional playback progress from 0.0f to 1.0f.
     */
    val progressFraction: Float
        get() = if (durationMs > 0) {
            (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Playback progress as a percentage from 0.0f to 100.0f.
     */
    val progressPercentage: Float
        get() = progressFraction * 100f
}

/**
 * Formats a millisecond duration into a clean "m:ss" time display.
 */
fun formatMediaDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

/**
 * Default empty idle track when no media is actively playing.
 */
val DEFAULT_MEDIA_TRACK = MediaTrack(
    title = "Not Playing",
    artist = "No active audio session",
    album = "",
    durationMs = 0L,
    positionMs = 0L,
    isPlaying = false
)
