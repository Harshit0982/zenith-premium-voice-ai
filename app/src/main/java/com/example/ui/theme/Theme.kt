package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val DarkColorScheme =
  darkColorScheme(
    primary = PurpleAccent,
    secondary = PinkAccent,
    tertiary = CyanAccent,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onPrimary = TextPrimary,
    onSecondary = TextPrimary,
    onTertiary = TextPrimary,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
  )

data class GlassThemeSystem(
    val borderBrush: Brush = Brush.linearGradient(
        colors = listOf(GlassBorder, Color.Transparent)
    ),
    val containerBrush: Brush = Brush.linearGradient(
        colors = listOf(SurfaceDark, SurfaceVariantDark)
    ),
    val shadowColor: Color = GlassShadow,
    val defaultRadius: Dp = 24.dp,
    val defaultBlur: Dp = 24.dp
)

val LocalGlassTheme = staticCompositionLocalOf { GlassThemeSystem() }

object GlassTheme {
    val current: GlassThemeSystem
        @Composable
        get() = LocalGlassTheme.current
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Force custom theme
  content: @Composable () -> Unit,
) {
  val glassTheme = GlassThemeSystem()
  
  CompositionLocalProvider(
    LocalGlassTheme provides glassTheme
  ) {
    MaterialTheme(
      colorScheme = DarkColorScheme, 
      typography = Typography, 
      content = content
    )
  }
}

