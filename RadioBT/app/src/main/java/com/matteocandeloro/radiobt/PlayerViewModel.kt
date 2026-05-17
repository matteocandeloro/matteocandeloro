package com.matteocandeloro.radiobt

import android.app.Application
import android.content.ComponentName
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class PlayerUiState(
    val currentTrack: Track? = null,
    val isPlaying: Boolean = false,
    val position: Long = 0L,
    val duration: Long = 0L,
    val queue: List<Track> = emptyList(),
    val currentIndex: Int = -1
)

class PlayerViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _tracks = MutableStateFlow<List<Track>>(emptyList())
    val tracks: StateFlow<List<Track>> = _tracks.asStateFlow()

    private val _isLoadingLibrary = MutableStateFlow(false)
    val isLoadingLibrary: StateFlow<Boolean> = _isLoadingLibrary.asStateFlow()

    private var controller: MediaController? = null
    private var positionPollingJob: Job? = null
    private val repository = MusicRepository(application)

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            updateUiState()
            if (isPlaying) startPositionPolling() else stopPositionPolling()
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            updateUiState()
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateUiState()
        }
    }

    init {
        connectToService()
        loadLibrary()
    }

    private fun connectToService() {
        val context = getApplication<Application>()
        val sessionToken = SessionToken(
            context,
            ComponentName(context, MusicService::class.java)
        )
        val controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture.addListener({
            val mediaController = controllerFuture.get()
            controller = mediaController
            mediaController.addListener(playerListener)
            updateUiState()
            if (mediaController.isPlaying) startPositionPolling()
        }, MoreExecutors.directExecutor())
    }

    fun loadLibrary() {
        viewModelScope.launch {
            _isLoadingLibrary.value = true
            try {
                _tracks.value = repository.loadTracks()
            } finally {
                _isLoadingLibrary.value = false
            }
        }
    }

    fun setPlaylist(tracks: List<Track>, startIndex: Int) {
        val ctrl = controller ?: return
        val mediaItems = tracks.map { track ->
            MediaItem.Builder()
                .setMediaId(track.id.toString())
                .setUri(track.uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(track.title)
                        .setArtist(track.artist)
                        .setAlbumTitle(track.album)
                        .setTrackNumber(track.trackNumber)
                        .setArtworkUri(track.albumArtUri)
                        .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                        .build()
                )
                .build()
        }

        ctrl.setMediaItems(mediaItems, startIndex, 0L)
        ctrl.prepare()
        ctrl.play()

        _uiState.value = _uiState.value.copy(queue = tracks, currentIndex = startIndex)
    }

    fun play(track: Track, allTracks: List<Track>) {
        val startIndex = allTracks.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
        setPlaylist(allTracks, startIndex)
    }

    fun togglePlayPause() {
        val ctrl = controller ?: return
        if (ctrl.isPlaying) ctrl.pause() else ctrl.play()
    }

    fun seekNext() {
        controller?.seekToNextMediaItem()
    }

    fun seekPrevious() {
        val ctrl = controller ?: return
        if (ctrl.currentPosition > 3000L) {
            ctrl.seekTo(0L)
        } else {
            ctrl.seekToPreviousMediaItem()
        }
    }

    fun seekTo(positionMs: Long) {
        controller?.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(position = positionMs)
    }

    private fun updateUiState() {
        val ctrl = controller ?: return
        val currentIndex = ctrl.currentMediaItemIndex
        val queueSnapshot = _uiState.value.queue

        val currentTrack = if (currentIndex in queueSnapshot.indices) {
            queueSnapshot[currentIndex]
        } else {
            null
        }

        _uiState.value = _uiState.value.copy(
            currentTrack = currentTrack,
            isPlaying = ctrl.isPlaying,
            position = ctrl.currentPosition.coerceAtLeast(0L),
            duration = ctrl.duration.coerceAtLeast(0L),
            currentIndex = currentIndex
        )
    }

    private fun startPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = viewModelScope.launch {
            while (isActive) {
                val ctrl = controller
                if (ctrl != null) {
                    _uiState.value = _uiState.value.copy(
                        position = ctrl.currentPosition.coerceAtLeast(0L),
                        duration = ctrl.duration.coerceAtLeast(0L)
                    )
                }
                delay(500L)
            }
        }
    }

    private fun stopPositionPolling() {
        positionPollingJob?.cancel()
        positionPollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopPositionPolling()
        controller?.removeListener(playerListener)
        controller?.release()
        controller = null
    }
}
