package com.example.bremautomotriz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.BremApplication
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.NuevaOtViewModel

@Composable
fun NuevaOtScreen(onVolver: () -> Unit) {
    val contexto = LocalContext.current
    val factoria = (contexto.applicationContext as BremApplication).contenedor.factoria
    val viewModel: NuevaOtViewModel = viewModel(factory = factoria)
    val estado by viewModel.uiState.collectAsState()

    PantallaBase(titulo = "Nueva orden de trabajo", onVolver = onVolver) {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = estado.patente,
                onValueChange = viewModel::onPatenteChanged,
                label = { Text("Patente") },
                leadingIcon = { Icon(Icons.Filled.DirectionsCar, contentDescription = null) },
                isError = estado.patenteError != null,
                supportingText = { Text(estado.patenteError ?: estado.vehiculoInfo.orEmpty()) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            if (estado.vehiculoNuevo) {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.nombreCliente, onValueChange = viewModel::onNombreClienteChanged,
                    label = { Text("Nombre del cliente") }, isError = estado.nombreClienteError != null,
                    supportingText = { estado.nombreClienteError?.let { Text(it) } },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = estado.run, onValueChange = viewModel::onRunChanged,
                    label = { Text("RUN del cliente (opcional)") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = estado.kilometraje,
                onValueChange = viewModel::onKilometrajeChanged,
                label = { Text("Kilometraje") },
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                isError = estado.kilometrajeError != null,
                supportingText = { estado.kilometrajeError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.observacion,
                onValueChange = viewModel::onObservacionChanged,
                label = { Text("Motivo de ingreso") },
                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                isError = estado.observacionError != null,
                supportingText = { estado.observacionError?.let { Text(it) } },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    viewModel.guardar { id ->
                        Toast.makeText(contexto, "Orden de trabajo N° $id creada", Toast.LENGTH_SHORT).show()
                        onVolver()
                    }
                },
                enabled = !estado.cargando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (estado.cargando) "Guardando..." else "Guardar orden")
            }
        }
    }
}
