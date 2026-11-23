package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.ClienteRepository
import com.example.bremautomotriz.data.repository.OrdenRepository
import com.example.bremautomotriz.data.repository.VehiculoRepository
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.Vehiculo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NuevaOtUiState(
    val patente: String = "",
    val patenteError: String? = null,
    val vehiculo: Vehiculo? = null,
    val vehiculoNuevo: Boolean = false,
    val vehiculoInfo: String? = null,
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val nombreCliente: String = "",
    val run: String = "",
    val kilometraje: String = "",
    val observacion: String = "",
    val marcaError: String? = null,
    val modeloError: String? = null,
    val anioError: String? = null,
    val nombreClienteError: String? = null,
    val kilometrajeError: String? = null,
    val observacionError: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class NuevaOtViewModel(
    private val vehiculoRepositorio: VehiculoRepository,
    private val clienteRepositorio: ClienteRepository,
    private val ordenRepositorio: OrdenRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NuevaOtUiState())
    val uiState: StateFlow<NuevaOtUiState> = _uiState.asStateFlow()

    private var busqueda: Job? = null

    fun onPatenteChanged(valor: String) {
        val patente = valor.trim().uppercase().take(6)
        _uiState.update {
            it.copy(
                patente = patente,
                patenteError = null,
                vehiculo = null,
                vehiculoNuevo = false,
                vehiculoInfo = null,
                error = null
            )
        }
        busqueda?.cancel()
        if (patente.length != 6) return
        busqueda = viewModelScope.launch {
            delay(300)
            when (val resultado = vehiculoRepositorio.porPatente(patente)) {
                is ApiResult.Ok -> _uiState.update {
                    val vehiculo = resultado.valor
                    it.copy(
                        vehiculo = vehiculo,
                        vehiculoNuevo = false,
                        vehiculoInfo = "${vehiculo.marca} ${vehiculo.modelo} ${vehiculo.anio} · ${vehiculo.clienteNombre.orEmpty()}"
                    )
                }
                is ApiResult.Fallo -> _uiState.update {
                    if (resultado.status == 404) {
                        it.copy(
                            vehiculoNuevo = true,
                            vehiculoInfo = "Patente no registrada. Completa los datos del vehículo y del cliente."
                        )
                    } else {
                        it.copy(error = resultado.mensaje)
                    }
                }
            }
        }
    }

    fun onMarcaChanged(valor: String) = _uiState.update { it.copy(marca = valor, marcaError = null) }
    fun onModeloChanged(valor: String) = _uiState.update { it.copy(modelo = valor, modeloError = null) }
    fun onAnioChanged(valor: String) = _uiState.update {
        it.copy(anio = valor.filter { c -> c.isDigit() }.take(4), anioError = null)
    }
    fun onNombreClienteChanged(valor: String) = _uiState.update {
        it.copy(nombreCliente = valor, nombreClienteError = null)
    }
    fun onRunChanged(valor: String) = _uiState.update { it.copy(run = valor.trim().take(12)) }
    fun onKilometrajeChanged(valor: String) = _uiState.update {
        it.copy(kilometraje = valor.filter { c -> c.isDigit() }.take(7), kilometrajeError = null)
    }
    fun onObservacionChanged(valor: String) = _uiState.update {
        it.copy(observacion = valor, observacionError = null)
    }

    fun guardar(onExito: (Long) -> Unit) {
        val estado = _uiState.value
        val patenteError = Validadores.patente(estado.patente)
        val kilometrajeError = Validadores.kilometraje(estado.kilometraje)
        val observacionError = Validadores.observacion(estado.observacion)
        val nuevo = estado.vehiculoNuevo
        val marcaError = if (nuevo) Validadores.texto(estado.marca, "la marca", 50) else null
        val modeloError = if (nuevo) Validadores.texto(estado.modelo, "el modelo", 50) else null
        val anioError = if (nuevo) Validadores.anio(estado.anio) else null
        val clienteError = if (nuevo) Validadores.texto(estado.nombreCliente, "el nombre del cliente") else null

        if (listOf(patenteError, kilometrajeError, observacionError, marcaError, modeloError, anioError, clienteError)
                .any { it != null }
        ) {
            _uiState.update {
                it.copy(
                    patenteError = patenteError,
                    kilometrajeError = kilometrajeError,
                    observacionError = observacionError,
                    marcaError = marcaError,
                    modeloError = modeloError,
                    anioError = anioError,
                    nombreClienteError = clienteError
                )
            }
            return
        }
        if (estado.vehiculo == null && !nuevo) {
            _uiState.update { it.copy(patenteError = "Busca la patente antes de continuar.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val vehiculoId = estado.vehiculo?.id ?: crearVehiculo(estado) ?: run {
                _uiState.update { it.copy(cargando = false, error = it.error ?: "No se pudo registrar el vehículo.") }
                return@launch
            }
            when (val resultado = ordenRepositorio.crear(
                vehiculoId,
                estado.kilometraje.toInt(),
                estado.observacion
            )) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(cargando = false) }
                    onExito(resultado.valor)
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    private suspend fun crearVehiculo(estado: NuevaOtUiState): Long? {
        val cliente = when (val resultado = clienteRepositorio.crear(
            estado.nombreCliente,
            estado.run.ifBlank { null },
            null,
            null
        )) {
            is ApiResult.Ok -> resultado.valor
            is ApiResult.Fallo -> {
                _uiState.update { it.copy(error = resultado.mensaje) }
                return null
            }
        }
        val vehiculo = when (val resultado = vehiculoRepositorio.crear(
            estado.patente,
            estado.marca,
            estado.modelo,
            estado.anio.toInt(),
            null,
            null,
            cliente.id
        )) {
            is ApiResult.Ok -> resultado.valor
            is ApiResult.Fallo -> {
                _uiState.update { it.copy(error = resultado.mensaje) }
                return null
            }
        }
        return vehiculo.id
    }
}
