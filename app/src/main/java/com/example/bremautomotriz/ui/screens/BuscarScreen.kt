package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun BuscarScreen(
    onVolver: () -> Unit,
    onAbrirOt: (Long) -> Unit
) {
    PantallaBase(titulo = "Buscar", onVolver = onVolver) {
        Text("Aquí irá la búsqueda por patente, cliente o N° de OT")
        Button(onClick = { onAbrirOt(1L) }, modifier = Modifier.fillMaxWidth()) {
            Text("Abrir OT de ejemplo")
        }
    }
}