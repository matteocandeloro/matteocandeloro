package com.matteocandeloro.radiobt.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AmoledDarkScheme = darkColorScheme(
    primary            = Color(0xFFBB86FC),
    onPrimary          = Color(0xFF21005D),
    primaryContainer   = Color(0xFF370096),
    onPrimaryContainer = Color(0xFFE9DDFF),
    secondary          = Color(0xFF90CAF9),
    onSecondary        = Color(0xFF003258),
    secondaryContainer = Color(0xFF1A3A5C),
    onSecondaryContainer = Color(0xFFD1E4FF),
    background         = Color(0xFF000000),   // true black — AMOLED power saving
    onBackground       = Color(0xFFE6E1E5),
    surface            = Color(0xFF0D0D0D),
    onSurface          = Color(0xFFE6E1E5),
    surfaceVariant     = Color(0xFF1C1C1C),
    onSurfaceVariant   = Color(0xFFCAC4D0),
    outline            = Color(0xFF555555),
    outlineVariant     = Color(0xFF2A2A2A),
)

@Composable
fun RadioBTTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Always AMOLED dark — ignores system setting and dynamic color
    MaterialTheme(
        colorScheme = AmoledDarkScheme,
        content = content
    )
}
