package com.example.bremautomotriz.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.domain.Rol
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onBuscar: () -> Unit,
    onNuevaOt: () -> Unit,
    onClientes: () -> Unit,
    onVehiculos: () -> Unit,
    onSalir: () -> Unit
) {
    val contexto = LocalContext.current
    val factoria = (contexto.applicationContext as BremApplication).contenedor.factoria
    val viewModel: HomeViewModel = viewModel(factory = factoria)
    val estado by viewModel.uiState.collectAsState()
    val usuario by viewModel.usuario.collectAsState()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    PantallaBase(titulo = "Inicio") {
        Aparece(visible, 0) {
            Text("Hola, ${usuario?.nombre.orEmpty()}", style = MaterialTheme.typography.headlineSmall)
        }
        Aparece(visible, 100) {
            Text(
                when (usuario?.rol) {
                    Rol.ADMIN -> "Administrador"
                    Rol.JEFE -> "Jefe de taller"
                    else -> ""
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Aparece(visible, 300) {
            Button(onClick = onBuscar, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Search, contentDescription = null)
                Text("  Buscar patente, cliente o N° de OT")
            }
        }
        Aparece(visible, 400) {
            Button(onClick = onNuevaOt, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  Nueva orden de trabajo")
            }
        }
        Aparece(visible, 500) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onClientes, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.People, contentDescription = null)
                    Text("  Clientes")
                }
                OutlinedButton(onClick = onVehiculos, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DirectionsCar, contentDescription = null)
                    Text("  Vehículos")
                }
            }
        }
        Aparece(visible, 600) {
            OutlinedButton(onClick = { viewModel.cerrarSesion(onSalir) }, modifier = Modifier.fillMaxWidth()) {
                Text("Cerrar sesión")
            }
        }
    }
}

@Composable
private fun Aparece(visible: Boolean, retraso: Int, contenido: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(400, delayMillis = retraso)) +
            slideInVertically(animationSpec = tween(400, delayMillis = retraso)) { it / 2 }
    ) {
        contenido()
    }
}
