package com.example.bremautomotriz.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = BremNaranja,
    onPrimary = Color.White,
    secondary = BremAzul,
    onSecondary = Color.White,
    background = FondoClaro
)

private val DarkColors = darkColorScheme(
    primary = BremNaranja,
    onPrimary = Color.White,
    secondary = BremAzulClaro,
    onSecondary = Color.Black,
    background = FondoOscuro
)

@Composable
fun BremAutomotrizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content
    )
}