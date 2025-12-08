package com.example.registroflytransportation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Colores principales de FLY T
val BlueStart = Color(0xFF6B8EFF)
val PurpleEnd = Color(0xFF8B6FB8)
val PrimaryBlue = Color(0xFF1976D2)
val White = Color.White
val Black = Color.Black

// Colores adicionales
val LightBlue = Color(0xFF5B7FEE)
val DarkBlue = Color(0xFF0D47A1)
val LightGray = Color(0xFFF5F5F5)
val MediumGray = Color(0xFFBDBDBD)
val DarkGray = Color(0xFF424242)

// Color de error
val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF4CAF50)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = White,
    secondary = BlueStart,
    onSecondary = White,
    tertiary = PurpleEnd,
    background = White,
    surface = White,
    error = ErrorRed
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueStart,
    onPrimary = White,
    secondary = PurpleEnd,
    onSecondary = White,
    tertiary = LightBlue,
    background = DarkGray,
    surface = DarkGray,
    error = ErrorRed
)

@Composable
fun RegistroFlyTransportationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}