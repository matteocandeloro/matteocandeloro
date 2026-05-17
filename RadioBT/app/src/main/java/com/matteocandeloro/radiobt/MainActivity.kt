package com.matteocandeloro.radiobt

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.matteocandeloro.radiobt.ui.LibraryScreen
import com.matteocandeloro.radiobt.ui.PlayerScreen
import com.matteocandeloro.radiobt.ui.RadioBTTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            viewModel.loadLibrary()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMediaPermissionsIfNeeded()

        setContent {
            RadioBTTheme {
                var showPlayer by rememberSaveable { mutableStateOf(false) }

                Scaffold(
                    topBar = { RadioBTTopBar(showPlayer = showPlayer, onBack = { showPlayer = false }) }
                ) { innerPadding ->
                    if (showPlayer) {
                        PlayerScreen(
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    } else {
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

    private fun requestMediaPermissionsIfNeeded() {
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

        val notGranted = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            viewModel.loadLibrary()
        } else {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun RadioBTTopBar(showPlayer: Boolean, onBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = if (showPlayer) "Now Playing" else "RadioBT")
        },
        navigationIcon = {
            if (showPlayer) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to library"
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.LibraryMusic,
                    contentDescription = null
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors()
    )
}
