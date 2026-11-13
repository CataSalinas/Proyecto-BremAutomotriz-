package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.data.entity.Cliente
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.data.entity.Vehiculo
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.NuevaOtModel
import kotlinx.coroutines.launch

class NuevaOtViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)

    var uiState by mutableStateOf(NuevaOtModel())
        private set

    fun onPatenteChanged(valor: String) {
        val patente = valor.trim().uppercase().take(6)
        uiState = uiState.copy(
            patente = patente,
            patenteError = null,
            vehiculoInfo = null,
            vehiculoNuevo = false
        )
        if (patente.length == 6) {
            viewModelScope.launch {
                val vehiculo = db.vehiculoDao().buscarPorPatente(patente)
                if (uiState.patente != patente) return@launch
                if (vehiculo != null) {
                    val cliente = db.clienteDao().buscarPorId(vehiculo.clienteId)
                    uiState = uiState.copy(
                        vehiculoInfo = "${vehiculo.marca} ${vehiculo.modelo} ${vehiculo.anio} · ${cliente?.nombre ?: ""}",
                        vehiculoNuevo = false
                    )
                } else {
                    // Patente no registrada: se pedirán los datos del vehículo y del cliente.
                    uiState = uiState.copy(
                        vehiculoInfo = "Vehículo nuevo: completa sus datos y los del cliente.",
                        vehiculoNuevo = true
                    )
                }
            }
        }
    }

    fun onMarcaChanged(v: String) { uiState = uiState.copy(marca = v, marcaError = null) }
    fun onModeloChanged(v: String) { uiState = uiState.copy(modelo = v, modeloError = null) }
    fun onAnioChanged(v: String) {
        uiState = uiState.copy(anio = v.filter { it.isDigit() }.take(4), anioError = null)
    }
    fun onNombreClienteChanged(v: String) {
        uiState = uiState.copy(nombreCliente = v, nombreClienteError = null)
    }
    fun onRunChanged(v: String) { uiState = uiState.copy(run = v.take(12)) }

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
        val nuevo = uiState.vehiculoNuevo
        val marcaError = if (nuevo) Validadores.texto(uiState.marca.trim(), "la marca") else null
        val modeloError = if (nuevo) Validadores.texto(uiState.modelo.trim(), "el modelo") else null
        val anioError = if (nuevo) Validadores.anio(uiState.anio) else null
        val clienteError = if (nuevo) Validadores.texto(uiState.nombreCliente.trim(), "el nombre del cliente") else null

        if (listOf(patenteError, kilometrajeError, observacionError, marcaError, modeloError, anioError, clienteError)
                .any { it != null }
        ) {
            uiState = uiState.copy(
                patenteError = patenteError,
                kilometrajeError = kilometrajeError,
                observacionError = observacionError,
                marcaError = marcaError,
                modeloError = modeloError,
                anioError = anioError,
                nombreClienteError = clienteError
            )
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(cargando = true)
            var vehiculo = db.vehiculoDao().buscarPorPatente(uiState.patente)
            val nombreCliente: String
            if (vehiculo == null) {
                if (!nuevo) {
                    // Aún no se detectaba que la patente era nueva: mostrar el formulario.
                    uiState = uiState.copy(
                        cargando = false,
                        vehiculoNuevo = true,
                        vehiculoInfo = "Vehículo nuevo: completa sus datos y los del cliente."
                    )
                    return@launch
                }
                val clienteId = db.clienteDao().insertar(
                    Cliente(nombre = uiState.nombreCliente.trim(), run = uiState.run.trim())
                )
                val vehiculoId = db.vehiculoDao().insertar(
                    Vehiculo(
                        patente = uiState.patente,
                        marca = uiState.marca.trim(),
                        modelo = uiState.modelo.trim(),
                        anio = uiState.anio.toInt(),
                        clienteId = clienteId
                    )
                )
                vehiculo = Vehiculo(
                    id = vehiculoId,
                    patente = uiState.patente,
                    marca = uiState.marca.trim(),
                    modelo = uiState.modelo.trim(),
                    anio = uiState.anio.toInt(),
                    clienteId = clienteId
                )
                nombreCliente = uiState.nombreCliente.trim()
            } else {
                nombreCliente = db.clienteDao().buscarPorId(vehiculo.clienteId)?.nombre ?: ""
            }
            val id = db.ordenTrabajoDao().insertar(
                OrdenTrabajo(
                    vehiculoId = vehiculo.id,
                    patente = vehiculo.patente,
                    nombreCliente = nombreCliente,
                    kilometraje = uiState.kilometraje.toInt(),
                    observacionIngreso = uiState.observacion.trim()
                )
            )
            uiState = uiState.copy(cargando = false)
            onExito(id)
        }
    }
}