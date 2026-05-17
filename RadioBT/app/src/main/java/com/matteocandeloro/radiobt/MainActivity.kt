package com.matteocandeloro.radiobt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.content.ContextCompat
import com.matteocandeloro.radiobt.ui.LibraryScreen
import com.matteocandeloro.radiobt.ui.PlayerScreen
import com.matteocandeloro.radiobt.ui.RadioBTTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) viewModel.loadLibrary()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissionsIfNeeded()

        setContent {
            RadioBTTheme {
                var showPlayer by rememberSaveable { mutableStateOf(false) }
                var brightness by remember { mutableFloatStateOf(-1f) }
                var screenOff by remember { mutableStateOf(false) }

                // Apply window brightness (no special permission needed — affects this window only)
                LaunchedEffect(brightness) {
                    val attrs = window.attributes
                    attrs.screenBrightness = brightness
                    window.attributes = attrs
                }

                AnimatedContent(
                    targetState = showPlayer,
                    transitionSpec = {
                        if (targetState) {
                            slideInVertically { it } togetherWith slideOutVertically { -it / 4 }
                        } else {
                            slideInVertically { -it / 4 } togetherWith slideOutVertically { it }
                        }
                    },
                    label = "screen_nav"
                ) { isPlayer ->
                    if (isPlayer) {
                        PlayerScreen(
                            viewModel = viewModel,
                            brightness = brightness,
                            onBrightnessChange = { brightness = it },
                            screenOff = screenOff,
                            onScreenOffToggle = { screenOff = !screenOff },
                            onBack = { showPlayer = false }
                        )
                    } else {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text("RadioBT") },
                                    navigationIcon = {
                                        Icon(
                                            imageVector = Icons.Default.LibraryMusic,
                                            contentDescription = null,
                                            tint = Color.White.copy(alpha = 0.7f)
                                        )
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors(
                                        containerColor = Color(0xFF0D0D0D),
                                        titleContentColor = Color.White
                                    )
                                )
                            },
                            containerColor = Color(0xFF000000)
                        ) { innerPadding ->
                            LibraryScreen(
                                viewModel = viewModel,
                                onTrackClick = { showPlayer = true },
                                onMiniPlayerClick = { showPlayer = true },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun requestPermissionsIfNeeded() {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }
        val needed = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isEmpty()) {
            viewModel.loadLibrary()
        } else {
            permissionLauncher.launch(needed.toTypedArray())
        }
    }
}
