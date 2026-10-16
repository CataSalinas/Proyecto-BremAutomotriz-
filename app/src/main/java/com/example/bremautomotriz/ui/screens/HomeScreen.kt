package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.data.entity.Rol
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun HomeScreen(
    onBuscar: () -> Unit,
    onNuevaOt: () -> Unit,
    onSalir: () -> Unit
) {
    val usuario = Sesion.usuario
    val rolTexto = when (usuario?.rol) {
        Rol.ADMIN -> "Administrador"
        Rol.JEFE -> "Jefe de taller"
        Rol.DEVELOPER -> "Developer"
        else -> ""
    }

    PantallaBase(titulo = "Inicio") {
        Text(
            text = "Hola, ${usuario?.nombre ?: ""}",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = rolTexto,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Text("  Buscar patente, cliente o N° de OT")
        }
        Button(onClick = onNuevaOt, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Text("  Nueva orden de trabajo")
        }
        OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión")
        }
    }
}