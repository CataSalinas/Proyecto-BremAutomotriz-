package com.example.bremautomotriz.ui.screens

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
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
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.data.Fotos
import com.example.bremautomotriz.domain.EstadoOt
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.domain.ResumenOt
import com.example.bremautomotriz.domain.Textos
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.model.DetalleOrden
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.DetalleOtUiState
import com.example.bremautomotriz.ui.viewmodel.DetalleOtViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun DetalleOtScreen(id: Long, onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val contenedor = (contexto.applicationContext as BremApplication).contenedor
    val viewModel: DetalleOtViewModel = viewModel(factory = contenedor.factoria)
    val estado by viewModel.uiState.collectAsState()
    val usuario by contenedor.sessionStore.sesion.collectAsState(initial = null)
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var dialogoEstado by remember { mutableStateOf<String?>(null) }

    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val ruta = rutaPendiente
        if (ruta != null) {
            if (exito) viewModel.agregarEvidencia(ruta) else File(ruta).delete()
        }
        rutaPendiente = null
    }

    LaunchedEffect(id) { viewModel.cargar(id) }

    PantallaBase(titulo = "Orden N° $id", onVolver = onVolver) {
        when {
            estado.cargando && estado.detalle == null -> CircularProgressIndicator()
            estado.error != null && estado.detalle == null -> {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                Button(onClick = viewModel::recargar) { Text("Reintentar") }
            }
            estado.detalle != null -> ContenidoDetalle(
                viewModel = viewModel,
                estado = estado,
                detalle = estado.detalle!!,
                modifier = Modifier.weight(1f),
                rol = usuario?.rol,
                onAnular = { dialogoEstado = EstadoOt.ANULADA },
                onEntregar = { dialogoEstado = EstadoOt.ENTREGADO },
                onFoto = {
                    val archivo = Fotos.nuevoArchivo(contexto, id)
                    rutaPendiente = archivo.absolutePath
                    camara.launch(Fotos.uriParaCamara(contexto, archivo))
                }
            )
        }
    }

    dialogoEstado?.let { estadoNuevo ->
        AlertDialog(
            onDismissRequest = { dialogoEstado = null },
            title = { Text(if (estadoNuevo == EstadoOt.ANULADA) "Anular orden" else "Entregar orden") },
            text = {
                if (estadoNuevo == EstadoOt.ENTREGADO) {
                    OutlinedTextField(
                        value = estado.observacionEntrega,
                        onValueChange = viewModel::onObservacionEntregaChanged,
                        label = { Text("Observación de entrega") },
                        isError = estado.observacionError != null,
                        supportingText = { estado.observacionError?.let { Text(it) } },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("¿Anular la orden N° $id? Esta acción no se puede revertir.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.cambiarEstado(estadoNuevo)
                    dialogoEstado = null
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = { dialogoEstado = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ContenidoDetalle(
    viewModel: DetalleOtViewModel,
    estado: DetalleOtUiState,
    detalle: DetalleOrden,
    modifier: Modifier,
    rol: String?,
    onAnular: () -> Unit,
    onEntregar: () -> Unit,
    onFoto: () -> Unit
) {
    val contexto = LocalContext.current
    val orden = detalle.orden
    val siguiente = ReglasOt.siguiente(orden.estado)
    val editable = ReglasOt.puedeEditar(orden.estado)
    val colorEstado by animateColorAsState(
        targetValue = when (orden.estado) {
            EstadoOt.PENDIENTE -> MaterialTheme.colorScheme.secondary
            EstadoOt.EN_PROCESO -> MaterialTheme.colorScheme.primary
            EstadoOt.ANULADA -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.tertiary
        },
        animationSpec = tween(500),
        label = "colorEstado"
    )

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AnimatedContent(
                        targetState = orden.estado,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "estadoOt"
                    ) { actual ->
                        Text(Textos.estado(actual), style = MaterialTheme.typography.titleLarge, color = colorEstado)
                    }
                    Text("Patente: ${orden.patente}")
                    Text("Cliente: ${orden.nombreCliente}")
                    Text("Kilometraje: ${orden.kilometraje} km")
                    Text("Ingreso: ${Textos.fecha(orden.fechaIngreso)}")
                    Text("Motivo: ${orden.observacionIngreso}")
                    orden.observacionEntrega?.let { Text("Entrega: $it") }
                }
            }
        }

        if (editable) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = estado.observacionIngreso,
                            onValueChange = viewModel::onObservacionIngresoChanged,
                            label = { Text("Editar observación de ingreso") },
                            isError = estado.observacionError != null,
                            supportingText = { estado.observacionError?.let { Text(it) } },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = viewModel::guardarObservacion, modifier = Modifier.fillMaxWidth()) {
                            Text("Guardar observación")
                        }
                    }
                }
            }
        }

        if (siguiente != null && ReglasOt.puedeAvanzar(orden.estado, rol)) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            if (siguiente == EstadoOt.ENTREGADO) onEntregar()
                            else viewModel.cambiarEstado(siguiente)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pasar a ${Textos.estado(siguiente)}")
                    }
                    if (ReglasOt.puedeAnular(orden.estado, rol)) {
                        OutlinedButton(onClick = onAnular, modifier = Modifier.weight(1f)) {
                            Text("Anular")
                        }
                    }
                }
            }
        }

        item { Text("Detalle de trabajos y repuestos", style = MaterialTheme.typography.titleMedium) }

        if (detalle.lineas.isEmpty()) {
            item { Text("Aún no hay líneas agregadas.") }
        }

        items(detalle.lineas, key = { "l${it.id}" }) { linea ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${Textos.tipoLinea(linea.tipo)} · ${linea.descripcion}")
                        Text("Costo ${Textos.pesos(linea.costo)} · Factor ${linea.factor}")
                        Text("Cobrado ${Textos.pesos(linea.cobrado)}", color = MaterialTheme.colorScheme.primary)
                    }
                    if (editable) {
                        IconButton(onClick = { viewModel.editarLinea(linea) }) {
                            Icon(Icons.Filled.Edit, contentDescription = "Editar línea")
                        }
                        IconButton(onClick = { viewModel.eliminarLinea(linea) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Eliminar línea")
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilaMonto("Neto", orden.neto)
                    FilaMonto("IVA", orden.iva)
                    FilaMonto("Total", orden.total, destacado = true)
                }
            }
        }

        item {
            OutlinedButton(
                onClick = {
                    val texto = ResumenOt.texto(orden, detalle.lineas, orden.iva, orden.total)
                    val envio = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Orden de trabajo N° ${orden.id} - BREM Automotriz")
                        putExtra(Intent.EXTRA_TEXT, texto)
                    }
                    contexto.startActivity(Intent.createChooser(envio, "Compartir orden"))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text("  Compartir orden")
            }
        }

        if (editable) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (estado.editandoLineaId == null) "Agregar línea" else "Editar línea",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TipoLinea.TODOS.forEach { tipo ->
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
                            label = { Text("Costo") },
                            isError = estado.montoError != null,
                            supportingText = { estado.montoError?.let { Text(it) } },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = estado.factor,
                            onValueChange = viewModel::onFactorChanged,
                            label = { Text("Factor (opcional)") },
                            isError = estado.factorError != null,
                            supportingText = { estado.factorError?.let { Text(it) } },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = viewModel::guardarLinea, modifier = Modifier.weight(1f)) {
                                Text(if (estado.editandoLineaId == null) "Agregar" else "Actualizar")
                            }
                            if (estado.editandoLineaId != null) {
                                OutlinedButton(onClick = viewModel::nuevaLinea, modifier = Modifier.weight(1f)) {
                                    Text("Cancelar")
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Text("Fotos del vehículo", style = MaterialTheme.typography.titleMedium) }
        item {
            Button(onClick = onFoto, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                Text("  Tomar foto")
            }
        }
        if (detalle.evidencias.isEmpty()) {
            item { Text("Aún no hay fotos de esta orden.") }
        }
        items(detalle.evidencias, key = { "e${it.id}" }) { evidencia ->
            FotoEvidencia(ruta = evidencia.ruta, fecha = evidencia.fecha)
        }

        item { Text("Historial de la orden", style = MaterialTheme.typography.titleMedium) }
        if (detalle.historial.isEmpty()) {
            item { Text("No hay cambios de estado registrados.") }
        }
        items(detalle.historial, key = { "h${it.id}" }) { cambio ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${Textos.estado(cambio.estadoAnterior)} → ${Textos.estado(cambio.estadoNuevo)}")
                    Text(Textos.fecha(cambio.fechaCambio))
                    cambio.observacion?.let { Text(it) }
                }
            }
        }

        estado.error?.let { item { Text(it, color = MaterialTheme.colorScheme.error) } }
        estado.mensaje?.let { item { Text(it, color = MaterialTheme.colorScheme.primary) } }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, valor: Int, destacado: Boolean = false) {
    val estilo = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyLarge
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = estilo)
        Text(Textos.pesos(valor), style = estilo)
    }
}

@Composable
private fun FotoEvidencia(ruta: String, fecha: String) {
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
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(ruta, modifier = Modifier.padding(12.dp))
            }
            Text(Textos.fecha(fecha), modifier = Modifier.padding(12.dp))
        }
    }
}
