package com.hoandesign.standby.service

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaPlaybackState(
    val trackTitle: String = "No Media Playing",
    val artistName: String = "",
    val albumArtBitmap: Bitmap? = null,
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val position: Long = 0L,
    val hasActiveSession: Boolean = false
)

class StandbyMediaListenerService : NotificationListenerService() {

    private lateinit var mediaSessionManager: MediaSessionManager
    private var activeController: MediaController? = null

    override fun onCreate() {
        super.onCreate()
        mediaSessionManager = getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        instance = this
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        registerMediaControllerCallback()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        requestRebind(ComponentName(this, StandbyMediaListenerService::class.java))
    }

    private fun registerMediaControllerCallback() {
        try {
            val componentName = ComponentName(this, StandbyMediaListenerService::class.java)
            mediaSessionManager.addOnActiveSessionsChangedListener({ controllers ->
                updateActiveController(controllers)
            }, componentName)
            
            updateActiveController(mediaSessionManager.getActiveSessions(componentName))
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun updateActiveController(controllers: List<MediaController>?) {
        activeController?.unregisterCallback(mediaControllerCallback)
        
        activeController = controllers?.firstOrNull()
        activeController?.registerCallback(mediaControllerCallback)
        
        updatePlaybackState()
    }

    private val mediaControllerCallback = object : MediaController.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            updatePlaybackState()
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            updatePlaybackState()
        }
    }

    private fun updatePlaybackState() {
        val controller = activeController
        if (controller == null) {
            _mediaState.value = MediaPlaybackState()
            return
        }

        val metadata = controller.metadata
        val playbackState = controller.playbackState

        val title = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Title"
        val artist = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
        val art = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: metadata?.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val isPlaying = playbackState?.state == PlaybackState.STATE_PLAYING
        val position = playbackState?.position ?: 0L

        _mediaState.value = MediaPlaybackState(
            trackTitle = title,
            artistName = artist,
            albumArtBitmap = art,
            isPlaying = isPlaying,
            duration = duration,
            position = position,
            hasActiveSession = true
        )
    }

    fun play() {
        activeController?.transportControls?.play()
    }

    fun pause() {
        activeController?.transportControls?.pause()
    }

    fun skipToNext() {
        activeController?.transportControls?.skipToNext()
    }

    fun skipToPrevious() {
        activeController?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        activeController?.transportControls?.seekTo(positionMs)
    }

    override fun onDestroy() {
        super.onDestroy()
        activeController?.unregisterCallback(mediaControllerCallback)
        if (instance == this) {
            instance = null
        }
    }

    companion object {
        private val _mediaState = MutableStateFlow(MediaPlaybackState())
        val mediaState: StateFlow<MediaPlaybackState> = _mediaState.asStateFlow()

        var instance: StandbyMediaListenerService? = null
            private set
    }
}
