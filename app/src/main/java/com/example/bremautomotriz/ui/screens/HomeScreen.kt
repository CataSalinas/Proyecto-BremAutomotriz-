package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun HomeScreen(
    onBuscar: () -> Unit,
    onNuevaOt: () -> Unit,
    onSalir: () -> Unit
) {
    PantallaBase(titulo = "Inicio") {
        Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) {
            Text("Buscar patente, cliente o N° de OT")
        }
        Button(onClick = onNuevaOt, modifier = Modifier.fillMaxWidth()) {
            Text("Nueva orden de trabajo")
        }
        OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}