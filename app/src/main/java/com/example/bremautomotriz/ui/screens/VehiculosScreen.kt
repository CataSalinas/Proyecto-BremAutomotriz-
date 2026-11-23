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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.model.Vehiculo
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.VehiculosViewModel

@Composable
fun VehiculosScreen(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val factoria = (contexto.applicationContext as BremApplication).contenedor.factoria
    val viewModel: VehiculosViewModel = viewModel(factory = factoria)
    val estado by viewModel.uiState.collectAsState()
    var menuAbierto by remember { mutableStateOf(false) }
    val clienteSeleccionado = estado.clientes.firstOrNull { it.id == estado.clienteId }

    PantallaBase(titulo = "Vehículos", onVolver = onVolver) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = viewModel::nuevo, modifier = Modifier.weight(1f)) { Text("Nuevo vehículo") }
            OutlinedButton(onClick = viewModel::cargar, modifier = Modifier.weight(1f)) { Text("Actualizar lista") }
        }

        when {
            estado.cargando -> CircularProgressIndicator()
            estado.error != null -> Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (estado.editandoId == null) "Nuevo vehículo" else "Editar vehículo",
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = estado.patente, onValueChange = viewModel::onPatenteChanged,
                    label = { Text("Patente") }, isError = estado.patenteError != null,
                    supportingText = { estado.patenteError?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.marca, onValueChange = viewModel::onMarcaChanged,
                    label = { Text("Marca") }, isError = estado.marcaError != null,
                    supportingText = { estado.marcaError?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.modelo, onValueChange = viewModel::onModeloChanged,
                    label = { Text("Modelo") }, isError = estado.modeloError != null,
                    supportingText = { estado.modeloError?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.anio, onValueChange = viewModel::onAnioChanged,
                    label = { Text("Año") }, isError = estado.anioError != null,
                    supportingText = { estado.anioError?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.vin, onValueChange = viewModel::onVinChanged,
                    label = { Text("VIN (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.tipo, onValueChange = viewModel::onTipoChanged,
                    label = { Text("Tipo (opcional)") }, singleLine = true, modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { menuAbierto = true }, modifier = Modifier.weight(1f)) {
                        Text(clienteSeleccionado?.nombre ?: "Seleccionar cliente")
                    }
                    DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                        estado.clientes.forEach { cliente ->
                            DropdownMenuItem(
                                text = { Text(cliente.nombre) },
                                onClick = {
                                    viewModel.onClienteIdChanged(cliente.id)
                                    menuAbierto = false
                                }
                            )
                        }
                    }
                }
                estado.clienteError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

                Button(
                    onClick = viewModel::guardar,
                    enabled = !estado.guardando,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (estado.guardando) "Guardando..." else "Guardar vehículo")
                }
                estado.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary) }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(estado.vehiculos, key = { it.id }) { vehiculo -> VehiculoItem(vehiculo, viewModel::editar) }
        }
    }
}

@Composable
private fun VehiculoItem(vehiculo: Vehiculo, onEditar: (Vehiculo) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("${vehiculo.patente} · ${vehiculo.marca} ${vehiculo.modelo}", style = MaterialTheme.typography.titleMedium)
                Text("Año ${vehiculo.anio} · Cliente: ${vehiculo.clienteNombre ?: vehiculo.clienteId}")
            }
            IconButton(onClick = { onEditar(vehiculo) }) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar vehículo")
            }
        }
    }
}
