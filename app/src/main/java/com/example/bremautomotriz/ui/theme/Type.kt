package com.example.bremautomotriz.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Jerarquía de títulos (equivalente a h1 > h2 > h3 > subtítulos).
// Todos los tamaños de encabezado/título se aumentaron de forma uniforme
// respecto a los valores por defecto de Material 3, manteniendo el contraste.
val Typography = Typography(
    displayLarge = TextStyle(
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontSize = 46.sp,
        lineHeight = 54.sp,
        fontWeight = FontWeight.Bold
    ),
    displaySmall = TextStyle(
        fontSize = 38.sp,
        lineHeight = 46.sp,
        fontWeight = FontWeight.Bold
    ),

    // h1 - encabezado principal de pantalla
    headlineLarge = TextStyle(
        fontSize = 36.sp,
        lineHeight = 44.sp,
        fontWeight = FontWeight.Bold
    ),
    // h2 - encabezado de sección
    headlineMedium = TextStyle(
        fontSize = 30.sp,
        lineHeight = 38.sp,
        fontWeight = FontWeight.Bold
    ),
    // h3 - título de pantalla / bloque (antes 24.sp)
    headlineSmall = TextStyle(
        fontSize = 26.sp,
        lineHeight = 33.sp,
        fontWeight = FontWeight.Bold
    ),

    // Títulos de tarjeta y estados (antes 22.sp)
    titleLarge = TextStyle(
        fontSize = 24.sp,
        lineHeight = 31.sp,
        fontWeight = FontWeight.SemiBold
    ),
    // Títulos de sección y filas (antes 16.sp)
    titleMedium = TextStyle(
        fontSize = 19.sp,
        lineHeight = 26.sp,
        fontWeight = FontWeight.SemiBold
    ),
    // Subtítulos (antes 14.sp)
    titleSmall = TextStyle(
        fontSize = 17.sp,
        lineHeight = 23.sp,
        fontWeight = FontWeight.SemiBold
    ),

    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontSize = 15.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),

    labelLarge = TextStyle(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium
    ),
    labelMedium = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Medium
    ),
    labelSmall = TextStyle(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium
    )
)
