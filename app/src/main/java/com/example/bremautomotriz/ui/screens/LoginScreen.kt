package com.example.bremautomotriz.ui.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bremautomotriz.ui.components.PantallaBase

@Composable
fun LoginScreen(onEntrar: () -> Unit) {
    PantallaBase(titulo = "BREM Automotriz") {
        Text("Inicio de sesiÃ³n")
        Button(onClick = onEntrar, modifier = Modifier.fillMaxWidth()) {
            Text("Entrar")
        }
    }
}