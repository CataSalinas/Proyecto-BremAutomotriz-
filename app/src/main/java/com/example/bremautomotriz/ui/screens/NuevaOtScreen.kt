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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.NuevaOtViewModel

@Composable
fun NuevaOtScreen(
    onVolver: () -> Unit,
    viewModel: NuevaOtViewModel = viewModel()
) {
    val estado = viewModel.uiState
    val context = LocalContext.current

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
                supportingText = { Text(estado.patenteError ?: estado.vehiculoInfo ?: "") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = estado.kilometraje,
                onValueChange = viewModel::onKilometrajeChanged,
                label = { Text("Kilometraje") },
                leadingIcon = { Icon(Icons.Filled.Speed, contentDescription = null) },
                isError = estado.kilometrajeError != null,
                supportingText = { estado.kilometrajeError?.let { Text(it) } },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next
                ),
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

            Button(
                onClick = {
                    viewModel.onGuardarClick { id ->
                        Toast.makeText(
                            context,
                            "Orden de trabajo NÂ° $id creada",
                            Toast.LENGTH_SHORT
                        ).show()
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