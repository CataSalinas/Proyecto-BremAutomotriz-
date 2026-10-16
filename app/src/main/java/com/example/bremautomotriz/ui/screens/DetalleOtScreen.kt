package com.example.bremautomotriz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.domain.Textos
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.DetalleOtViewModel

@Composable
fun DetalleOtScreen(
    id: Long,
    onVolver: () -> Unit,
    viewModel: DetalleOtViewModel = viewModel()
) {
    val estado = viewModel.uiState
    val context = LocalContext.current
    val orden = estado.orden

    LaunchedEffect(id) { viewModel.cargar(id) }

    PantallaBase(titulo = "Orden N° $id", onVolver = onVolver) {
        if (orden == null) {
            Text("Cargando...")
        } else {
            val siguiente = ReglasOt.siguiente(orden.estado)
            val puedeAvanzar = ReglasOt.puedeAvanzar(orden.estado, Sesion.usuario?.rol)
            val editable = ReglasOt.puedeEditarLineas(orden.estado)

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = Textos.estado(orden.estado),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Patente: ${orden.patente}")
                            Text("Cliente: ${orden.nombreCliente}")
                            Text("Kilometraje: ${orden.kilometraje} km")
                            Text("Ingreso: ${Textos.fecha(orden.fechaIngreso)}")
                            Text("Motivo: ${orden.observacionIngreso}")
                        }
                    }
                }

                if (siguiente != null) {
                    item {
                        if (puedeAvanzar) {
                            Button(
                                onClick = {
                                    viewModel.onAvanzarEstado { nuevo ->
                                        Toast.makeText(
                                            context,
                                            "Estado actualizado: ${Textos.estado(nuevo)}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Pasar a ${Textos.estado(siguiente)}")
                            }
                        } else {
                            Text("Solo un Administrador puede marcar la orden como Lista.")
                        }
                    }
                }

                item {
                    Text(
                        text = "Detalle de trabajos y repuestos",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (estado.lineas.isEmpty()) {
                    item { Text("Aún no hay líneas agregadas.") }
                }

                items(estado.lineas, key = { it.id }) { linea ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${Textos.tipoLinea(linea.tipo)} · ${linea.descripcion}")
                                if (linea.tipo == TipoLinea.OMEX || linea.tipo == TipoLinea.REPUESTO) {
                                    Text("Costo ${Textos.pesos(linea.costo)}")
                                }
                                Text(
                                    text = "Cobrado ${Textos.pesos(linea.cobrado)}",
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (editable) {
                                IconButton(onClick = { viewModel.onEliminarLinea(linea) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar línea")
                                }
                            }
                        }
                    }
                }

                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilaMonto("Neto", estado.neto)
                            FilaMonto("IVA (19%)", estado.iva)
                            FilaMonto("Total", estado.total, destacado = true)
                        }
                    }
                }

                if (editable) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Agregar línea",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(
                                    TipoLinea.OM,
                                    TipoLinea.OMEX,
                                    TipoLinea.REPUESTO,
                                    TipoLinea.INSUMO
                                ).forEach { tipo ->
                                    FilterChip(
                                        selected = estado.tipo == tipo,
                                        onClick = { viewModel.onTipoChanged(tipo) },
                                        label = { Text(Textos.tipoLinea(tipo)) }
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = estado.descripcion,
                                onValueChange = viewModel::onDescripcionChanged,
                                label = { Text("Descripción") },
                                isError = estado.descripcionError != null,
                                supportingText = { estado.descripcionError?.let { Text(it) } },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = estado.monto,
                                onValueChange = viewModel::onMontoChanged,
                                label = { Text("Monto (costo)") },
                                isError = estado.montoError != null,
                                supportingText = { estado.montoError?.let { Text(it) } },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = viewModel::onAgregarLinea,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Add, contentDescription = null)
                                Text("  Agregar línea")
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Historial del vehículo",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                if (estado.historial.isEmpty()) {
                    item { Text("No hay otras órdenes para este vehículo.") }
                }

                items(estado.historial, key = { "h${it.id}" }) { anterior ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("OT N° ${anterior.id} · ${Textos.fecha(anterior.fechaIngreso)}")
                            Text(anterior.observacionIngreso)
                            Text(
                                text = "${Textos.estado(anterior.estado)} · ${Textos.pesos(anterior.total)}",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: Int, destacado: Boolean = false) {
    val estilo = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = estilo)
        Text(Textos.pesos(valor), style = estilo)
    }
}