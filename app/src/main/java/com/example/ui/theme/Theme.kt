package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryPinkLight,
    onPrimary = Color(0xFF66002B),
    primaryContainer = PrimaryPink,
    onPrimaryContainer = Color(0xFFFFD9DF),
    secondary = SecondaryGold,
    onSecondary = Color(0xFF3C2F00),
    secondaryContainer = SecondaryGoldDim,
    onSecondaryContainer = Color(0xFFFFE088),
    background = BackgroundDark,
    onBackground = OnSurfaceWhite,
    surface = BackgroundDark,
    onSurface = OnSurfaceWhite,
    surfaceVariant = SurfaceContainerHighest,
    onSurfaceVariant = OnSurfaceVariantPink,
    outline = OnSurfaceVariantPink,
    outlineVariant = OutlinePink,
    error = ErrorRed,
    errorContainer = ErrorContainerRed,
    onError = Color(0xFF690005),
    onErrorContainer = Color(0xFFFFDAD6)
  )

private val LightColorScheme = DarkColorScheme // Queen Dance is dark-mode first for dramatic, premium studio feel

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is disabled to strictly respect custom color palette
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
