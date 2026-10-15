package com.example.bremautomotriz.ui.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun DetalleOtScreen(id: Long, onVolver: () -> Unit) {
    PantallaBase(titulo = "OT N° $id", onVolver = onVolver) {
        Text("Aquí irá el detalle de la orden de trabajo $id")
    }
}