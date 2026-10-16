package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.NuevaOtModel
import kotlinx.coroutines.launch

class NuevaOtViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)

    var uiState by mutableStateOf(NuevaOtModel())
        private set

    fun onPatenteChanged(valor: String) {
        val patente = valor.trim().uppercase().take(6)
        uiState = uiState.copy(patente = patente, patenteError = null, vehiculoInfo = null)
        if (patente.length == 6) {
            viewModelScope.launch {
                val vehiculo = db.vehiculoDao().buscarPorPatente(patente)
                if (vehiculo != null && uiState.patente == patente) {
                    val cliente = db.clienteDao().buscarPorId(vehiculo.clienteId)
                    uiState = uiState.copy(
                        vehiculoInfo = "${vehiculo.marca} ${vehiculo.modelo} ${vehiculo.anio} · ${cliente?.nombre ?: ""}"
                    )
                }
            }
        }
    }

    fun onKilometrajeChanged(valor: String) {
        uiState = uiState.copy(
            kilometraje = valor.filter { it.isDigit() }.take(7),
            kilometrajeError = null
        )
    }

    fun onObservacionChanged(valor: String) {
        uiState = uiState.copy(observacion = valor, observacionError = null)
    }

    fun onGuardarClick(onExito: (Long) -> Unit) {
        val patenteError = Validadores.patente(uiState.patente)
        val kilometrajeError = Validadores.kilometraje(uiState.kilometraje)
        val observacionError = Validadores.observacion(uiState.observacion)

        if (patenteError != null || kilometrajeError != null || observacionError != null) {
            uiState = uiState.copy(
                patenteError = patenteError,
                kilometrajeError = kilometrajeError,
                observacionError = observacionError
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            val vehiculo = db.vehiculoDao().buscarPorPatente(uiState.patente)
            if (vehiculo == null) {
                uiState = uiState.copy(
                    cargando = false,
                    patenteError = "La patente no está registrada."
                )
                return@launch
            }
            val cliente = db.clienteDao().buscarPorId(vehiculo.clienteId)
            val id = db.ordenTrabajoDao().insertar(
                OrdenTrabajo(
                    vehiculoId = vehiculo.id,
                    patente = vehiculo.patente,
                    nombreCliente = cliente?.nombre ?: "",
                    kilometraje = uiState.kilometraje.toInt(),
                    observacionIngreso = uiState.observacion.trim()
                )
            )
            uiState = uiState.copy(cargando = false)
            onExito(id)
        }
    }
}