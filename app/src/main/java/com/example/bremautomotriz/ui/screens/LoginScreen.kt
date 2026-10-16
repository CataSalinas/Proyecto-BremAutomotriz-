package com.example.bremautomotriz.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bremautomotriz.ui.components.PantallaBase
import com.example.bremautomotriz.ui.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onEntrar: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado = viewModel.uiState
    val context = LocalContext.current
    var verPassword by remember { mutableStateOf(false) }

    PantallaBase(titulo = "BREM Automotriz") {
        Text(
            text = "Inicio de sesión",
            style = MaterialTheme.typography.headlineSmall
        )

        OutlinedTextField(
            value = estado.email,
            onValueChange = viewModel::onEmailChanged,
            label = { Text("Correo") },
            leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
            isError = estado.emailError != null,
            supportingText = { estado.emailError?.let { Text(it) } },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = estado.password,
            onValueChange = viewModel::onPasswordChanged,
            label = { Text("Contraseña") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { verPassword = !verPassword }) {
                    Icon(
                        imageVector = if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                    )
                }
            },
            isError = estado.passwordError != null,
            supportingText = { estado.passwordError?.let { Text(it) } },
            singleLine = true,
            visualTransformation = if (verPassword) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )

        estado.errorGeneral?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                viewModel.onLoginClick {
                    Toast.makeText(context, "Sesión iniciada", Toast.LENGTH_SHORT).show()
                    onEntrar()
                }
            },
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (estado.cargando) "Ingresando..." else "Ingresar")
        }
    }
}