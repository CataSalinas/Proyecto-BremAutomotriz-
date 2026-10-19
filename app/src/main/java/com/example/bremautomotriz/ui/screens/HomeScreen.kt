package com.example.bremautomotriz.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    PantallaBase(titulo = "Inicio") {
        Aparece(visible = visible, retraso = 0) {
            Text(
                text = "Hola, ${usuario?.nombre ?: ""}",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        Aparece(visible = visible, retraso = 100) {
            Text(
                text = rolTexto,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Aparece(visible = visible, retraso = 200) {
            Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Text("  Buscar patente, cliente o N° de OT")
            }
        }
        Aparece(visible = visible, retraso = 300) {
            Button(onClick = onNuevaOt, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  Nueva orden de trabajo")
            }
        }
        Aparece(visible = visible, retraso = 400) {
            OutlinedButton(onClick = onSalir, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun Aparece(
    visible: Boolean,
    retraso: Int,
    contenido: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = retraso)) +
            slideInVertically(animationSpec = tween(400, delayMillis = retraso)) { it / 2 }
    ) {
        contenido()
    }
}