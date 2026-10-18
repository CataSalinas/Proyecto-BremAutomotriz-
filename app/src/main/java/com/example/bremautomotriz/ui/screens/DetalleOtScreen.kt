package com.example.bremautomotriz.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.data.Fotos
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.domain.ResumenOt
import com.example.bremautomotriz.domain.Textos
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.DetalleOtViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DetalleOtScreen(
    id: Long,
    onVolver: () -> Unit,
    viewModel: DetalleOtViewModel = viewModel()
) {
    val estado = viewModel.uiState
    val context = LocalContext.current
    val orden = estado.orden
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        if (ruta != null) {
            if (exito) {
                viewModel.onFotoTomada(ruta)
                Toast.makeText(context, "Foto agregada a la orden", Toast.LENGTH_SHORT).show()
            } else {
                File(ruta).delete()
            }
        }
        rutaPendiente = null
    }

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

                item {
                    OutlinedButton(
                        onClick = {
                            val texto = ResumenOt.texto(
                                orden,
                                estado.lineas,
                                estado.neto,
                                estado.iva,
                                estado.total
                            )
                            val envio = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Orden de trabajo N° ${orden.id} - BREM Automotriz"
                                )
                                putExtra(Intent.EXTRA_TEXT, texto)
                            }
                            context.startActivity(Intent.createChooser(envio, "Compartir orden"))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null)
                        Text("  Compartir orden")
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
                        text = "Fotos del vehículo",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                item {
                    Button(
                        onClick = {
                            val archivo = Fotos.nuevoArchivo(context, id)
                            rutaPendiente = archivo.absolutePath
                            camara.launch(Fotos.uriParaCamara(context, archivo))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                        Text("  Tomar foto")
                    }
                }

                if (estado.evidencias.isEmpty()) {
                    item { Text("Aún no hay fotos de esta orden.") }
                }

                items(estado.evidencias, key = { "e${it.id}" }) { evidencia ->
                    FotoEvidencia(ruta = evidencia.uri, fecha = evidencia.fecha)
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

@Composable
private fun FotoEvidencia(ruta: String, fecha: Long) {
    val imagen by produceState<ImageBitmap?>(initialValue = null, ruta) {
        value = withContext(Dispatchers.IO) { Fotos.cargar(ruta)?.asImageBitmap() }
    }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            val bitmap = imagen
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = "Foto del vehículo",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text("Cargando foto...", modifier = Modifier.padding(12.dp))
            }
            Text(Textos.fecha(fecha), modifier = Modifier.padding(12.dp))
        }
    }
}