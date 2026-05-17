package com.matteocandeloro.radiobt.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.BlurTransformation
import com.matteocandeloro.radiobt.PlayerViewModel

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    brightness: Float,
    onBrightnessChange: (Float) -> Unit,
    screenOff: Boolean,
    onScreenOffToggle: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val track = uiState.currentTrack
    val context = LocalContext.current

    // Cover art subtly scales up when playing
    val artScale by animateFloatAsState(
        targetValue = if (uiState.isPlaying) 1f else 0.90f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "art_scale"
    )

    Box(modifier = modifier.fillMaxSize()) {

        // ── Blurred album art as full-screen background ──────────────────
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(track?.albumArtUri)
                .transformations(BlurTransformation(context, radius = 25f, sampling = 4f))
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark radial overlay — lighter behind the cover, very dark at edges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Black.copy(alpha = 0.93f)
                        )
                    )
                )
        )

        // ── Main content column ──────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Back to library",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "NOW PLAYING",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.55f),
                        letterSpacing = 2.sp
                    )
                    if (uiState.currentIndex >= 0 && uiState.queue.isNotEmpty()) {
                        Text(
                            text = "${uiState.currentIndex + 1} / ${uiState.queue.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.30f)
                        )
                    }
                }
                IconButton(onClick = onScreenOffToggle) {
                    Icon(
                        Icons.Default.NightlightRound,
                        contentDescription = "Toggle display off",
                        tint = if (screenOff) Color(0xFFBB86FC) else Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            // ── Large album art ──────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .aspectRatio(1f)
                    .graphicsLayer { scaleX = artScale; scaleY = artScale }
                    .shadow(
                        elevation = 48.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = Color.Black,
                        spotColor = Color.Black.copy(alpha = 0.9f)
                    )
                    .clip(RoundedCornerShape(28.dp))
            ) {
                if (track?.albumArtUri != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(track.albumArtUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Album cover for ${track.album}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF1E1E2E), Color(0xFF2D1B69))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.size(96.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.weight(0.7f))

            // ── Track info ────────────────────────────────────────────────
            Text(
                text = track?.title ?: "Nothing playing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = track?.artist ?: "",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.70f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = track?.album ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.42f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.weight(0.7f))

            // ── Progress bar ─────────────────────────────────────────────
            var dragValue by remember { mutableStateOf<Float?>(null) }
            val sliderPos = dragValue ?: if (uiState.duration > 0L) {
                (uiState.position.toFloat() / uiState.duration.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Slider(
                value = sliderPos,
                onValueChange = { dragValue = it },
                onValueChangeFinished = {
                    dragValue?.let { v ->
                        if (uiState.duration > 0L) viewModel.seekTo((v * uiState.duration).toLong())
                    }
                    dragValue = null
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.duration > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                    disabledThumbColor = Color.White.copy(alpha = 0.3f),
                    disabledActiveTrackColor = Color.White.copy(alpha = 0.3f)
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-6).dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(uiState.position),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.50f)
                )
                Text(
                    text = formatDuration(uiState.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.50f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // ── Transport controls ────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.seekPrevious() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                FilledIconButton(
                    onClick = { viewModel.togglePlayPause() },
                    modifier = Modifier.size(70.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.seekNext() },
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Next",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(Modifier.weight(0.6f))

            // ── Brightness slider ─────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Default.BrightnessLow,
                    contentDescription = "Dim",
                    tint = Color.White.copy(alpha = 0.45f),
                    modifier = Modifier.size(18.dp)
                )
                Slider(
                    value = if (brightness < 0f) 0.5f else brightness,
                    onValueChange = { onBrightnessChange(it.coerceAtLeast(0.01f)) },
                    valueRange = 0.01f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White.copy(alpha = 0.75f),
                        activeTrackColor = Color.White.copy(alpha = 0.50f),
                        inactiveTrackColor = Color.White.copy(alpha = 0.12f)
                    )
                )
                Icon(
                    Icons.Default.BrightnessHigh,
                    contentDescription = "Bright",
                    tint = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // ── Screen-off overlay ────────────────────────────────────────────
        // Covers everything in pure black; tap anywhere to restore the display.
        if (screenOff) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable(onClick = onScreenOffToggle),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "tap to wake",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.10f),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}
