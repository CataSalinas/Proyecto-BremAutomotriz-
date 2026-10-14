package com.example.bremautomotriz.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun NuevaOtScreen(onVolver: () -> Unit) {
    PantallaBase(titulo = "Nueva OT", onVolver = onVolver) {
        Text("Aquí irá el formulario de nueva orden de trabajo")
    }
}