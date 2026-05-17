package com.matteocandeloro.radiobt

import android.net.Uri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val trackNumber: Int,
    val duration: Long,    // ms
    val uri: Uri,
    val albumArtUri: Uri?
)
