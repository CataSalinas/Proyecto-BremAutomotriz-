package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.model.Cliente
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.ClientesViewModel

@Composable
fun ClientesScreen(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val factoria = (contexto.applicationContext as BremApplication).contenedor.factoria
    val viewModel: ClientesViewModel = viewModel(factory = factoria)
    val estado by viewModel.uiState.collectAsState()

    PantallaBase(titulo = "Clientes", onVolver = onVolver) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = viewModel::nuevo, modifier = Modifier.weight(1f)) { Text("Nuevo cliente") }
            OutlinedButton(onClick = viewModel::cargar, modifier = Modifier.weight(1f)) { Text("Actualizar lista") }
        }

        when {
            estado.cargando -> CircularProgressIndicator()
            estado.error != null -> Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (estado.editandoId == null) "Nuevo cliente" else "Editar cliente",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = estado.nombre,
                    onValueChange = viewModel::onNombreChanged,
                    label = { Text("Nombre") },
                    isError = estado.nombreError != null,
                    supportingText = { estado.nombreError?.let { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.run, onValueChange = viewModel::onRunChanged,
                    label = { Text("RUN (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.telefono, onValueChange = viewModel::onTelefonoChanged,
                    label = { Text("Teléfono (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.correo, onValueChange = viewModel::onCorreoChanged,
                    label = { Text("Correo (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = viewModel::guardar,
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardando) "Guardando..." else "Guardar cliente")
                }
                estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estado.clientes, key = { it.id }) { cliente -> ClienteItem(cliente, viewModel::editar) }
        }
    }
}

@Composable
private fun ClienteItem(cliente: Cliente, onEditar: (Cliente) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(cliente.nombre, style = MaterialTheme.typography.titleMedium)
                cliente.run?.let { Text("RUN: $it") }
                cliente.telefono?.let { Text(it) }
            }
            IconButton(onClick = { onEditar(cliente) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar cliente")
            }
        }
    }
}
