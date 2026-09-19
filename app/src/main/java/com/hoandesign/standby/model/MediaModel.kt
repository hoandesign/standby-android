package com.hoandesign.standby.model

import java.util.Locale

/**
 * Immutable representation of a music or media playback track.
 *
 * @param title Track name (default: "Midnight City").
 * @param artist Performing artist name (default: "M83").
 * @param album Album name (default: "Hurry Up, We're Dreaming").
 * @param durationMs Total duration of track in milliseconds (default: 244_000 ms / 4:04).
 * @param positionMs Current playback position in milliseconds (default: 88_000 ms / 1:28).
 * @param isPlaying True if track is actively playing, false if paused.
 */
data class MediaTrack(
    val title: String = "Midnight City",
    val artist: String = "M83",
    val album: String = "Hurry Up, We're Dreaming",
    val durationMs: Long = 244_000L,
    val positionMs: Long = 88_000L,
    val isPlaying: Boolean = true
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
 * Built-in default sample track for instant aesthetic out-of-the-box experience.
 */
val DEFAULT_MEDIA_TRACK = MediaTrack(
    title = "Midnight City",
    artist = "M83",
    album = "Hurry Up, We're Dreaming",
    durationMs = 244_000L,
    positionMs = 88_000L,
    isPlaying = true
)
