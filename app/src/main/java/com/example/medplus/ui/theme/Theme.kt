package com.example.medplus.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MedPlusColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryLight,
    onPrimaryContainer = Color.White,
    
    secondary = Secondary,
    onSecondary = Color.White,
    
    background = Background,
    onBackground = PrimaryText,
    
    surface = Surface,
    onSurface = PrimaryText,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = SecondaryText,
    
    outline = Outline,
    
    error = Error,
    onError = Color.White
)

@Composable
fun MedPlusTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MedPlusColorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
