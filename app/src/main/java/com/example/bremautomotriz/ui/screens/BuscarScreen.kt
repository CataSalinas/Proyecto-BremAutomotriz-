package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.domain.Textos
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.BuscarViewModel

@Composable
fun BuscarScreen(onVolver: () -> Unit, onAbrirOt: (Long) -> Unit) {
    val contexto = LocalContext.current
    val factoria = (contexto.applicationContext as BremApplication).contenedor.factoria
    val viewModel: BuscarViewModel = viewModel(factory = factoria)
    val estado by viewModel.uiState.collectAsState()

    PantallaBase(titulo = "Buscar", onVolver = onVolver) {
        OutlinedTextField(
            value = estado.texto,
            onValueChange = viewModel::onTextoChanged,
            label = { Text("Patente, cliente o N° de OT") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        when {
            estado.cargando -> CircularProgressIndicator()
            estado.error != null -> {
                Text(estado.error.orEmpty(), color = MaterialTheme.colorScheme.error)
                TextButton(onClick = viewModel::reintentar) { Text("Reintentar") }
            }
            estado.resultados.isEmpty() -> Text("No se encontraron órdenes de trabajo.")
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(estado.resultados, key = { it.otId }) { ot ->
                Card(onClick = { onAbrirOt(ot.otId) }, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("OT N° ${ot.otId} · ${ot.patente}", style = MaterialTheme.typography.titleMedium)
                        Text(ot.nombreCliente)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Text(Textos.estado(ot.estado), color = MaterialTheme.colorScheme.primary)
                            Text("  ${Textos.pesos(ot.total)}", modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
