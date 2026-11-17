package com.example.bremautomotriz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.domain.Textos
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.BuscarViewModel

@Composable
fun BuscarScreen(
    onVolver: () -> Unit,
    onAbrirOt: (Long) -> Unit,
    viewModel: BuscarViewModel = viewModel()
) {
    val estado = viewModel.uiState
    val context = LocalContext.current
    val puedeEliminar = ReglasOt.puedeEliminar(Sesion.usuario?.rol)
    var ordenAEliminar by remember { mutableStateOf<OrdenTrabajo?>(null) }

    PantallaBase(titulo = "Buscar", onVolver = onVolver) {
        OutlinedTextField(
            value = estado.texto,
            onValueChange = viewModel::onTextoChanged,
            label = { Text("Patente, cliente o N° de OT") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        if (estado.resultados.isEmpty()) {
            Text("No se encontraron órdenes de trabajo.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estado.resultados, key = { it.id }) { ot ->
                Card(
                    onClick = { onAbrirOt(ot.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "OT N° ${ot.id} · ${ot.patente}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(ot.nombreCliente)
                            Text(
                                text = Textos.estado(ot.estado),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (puedeEliminar) {
                            IconButton(onClick = { ordenAEliminar = ot }) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Eliminar orden N° ${ot.id}"
                                )
                            }
                        }
                    }
                }
            }
        }

        ordenAEliminar?.let { orden ->
            AlertDialog(
                onDismissRequest = { ordenAEliminar = null },
                title = { Text("Eliminar orden de trabajo") },
                text = {
                    Text(
                        "¿Estás seguro de que quieres eliminar la orden N° ${orden.id} " +
                            "de la patente ${orden.patente}? Esta acción no se puede deshacer."
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            ordenAEliminar = null
                            viewModel.onEliminar(orden) {
                                Toast.makeText(
                                    context,
                                    "Orden N° ${orden.id} eliminada",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Eliminar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { ordenAEliminar = null }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
