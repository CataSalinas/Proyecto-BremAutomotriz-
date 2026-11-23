package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.ClienteRepository
import com.example.bremautomotriz.data.repository.VehiculoRepository
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.Cliente
import com.example.bremautomotriz.model.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class VehiculosUiState(
    val vehiculos: List<Vehiculo> = emptyList(),
    val clientes: List<Cliente> = emptyList(),
    val patente: String = "",
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val vin: String = "",
    val tipo: String = "",
    val clienteId: Long = 0,
    val patenteError: String? = null,
    val marcaError: String? = null,
    val modeloError: String? = null,
    val anioError: String? = null,
    val clienteError: String? = null,
    val editandoId: Long? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class VehiculosViewModel(
    private val vehiculoRepositorio: VehiculoRepository,
    private val clienteRepositorio: ClienteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VehiculosUiState())
    val uiState: StateFlow<VehiculosUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val vehiculos = vehiculoRepositorio.listar()
            val clientes = clienteRepositorio.listar()
            when {
                vehiculos is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = vehiculos.mensaje)
                }
                clientes is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = clientes.mensaje)
                }
                else -> _uiState.update {
                    it.copy(
                        cargando = false,
                        vehiculos = (vehiculos as ApiResult.Ok).valor,
                        clientes = (clientes as ApiResult.Ok).valor
                    )
                }
            }
        }
    }

    fun onPatenteChanged(valor: String) = _uiState.update {
        it.copy(patente = valor.trim().uppercase().take(6), patenteError = null)
    }
    fun onMarcaChanged(valor: String) = _uiState.update { it.copy(marca = valor, marcaError = null) }
    fun onModeloChanged(valor: String) = _uiState.update { it.copy(modelo = valor, modeloError = null) }
    fun onAnioChanged(valor: String) = _uiState.update {
        it.copy(anio = valor.filter { c -> c.isDigit() }.take(4), anioError = null)
    }
    fun onVinChanged(valor: String) = _uiState.update { it.copy(vin = valor.trim().take(17)) }
    fun onTipoChanged(valor: String) = _uiState.update { it.copy(tipo = valor.trim().take(30)) }
    fun onClienteIdChanged(valor: Long) = _uiState.update { it.copy(clienteId = valor, clienteError = null) }

    fun editar(vehiculo: Vehiculo) = _uiState.update {
        it.copy(
            editandoId = vehiculo.id,
            patente = vehiculo.patente,
            marca = vehiculo.marca,
            modelo = vehiculo.modelo,
            anio = vehiculo.anio.toString(),
            vin = vehiculo.vin.orEmpty(),
            tipo = vehiculo.tipo.orEmpty(),
            clienteId = vehiculo.clienteId,
            patenteError = null,
            marcaError = null,
            modeloError = null,
            anioError = null,
            clienteError = null,
            error = null,
            mensaje = null
        )
    }

    fun nuevo() = _uiState.update {
        it.copy(
            editandoId = null,
            patente = "",
            marca = "",
            modelo = "",
            anio = "",
            vin = "",
            tipo = "",
            clienteId = 0,
            patenteError = null,
            marcaError = null,
            modeloError = null,
            anioError = null,
            clienteError = null,
            error = null,
            mensaje = null
        )
    }

    fun guardar() {
        val estado = _uiState.value
        val patenteError = Validadores.patente(estado.patente)
        val marcaError = Validadores.texto(estado.marca, "la marca", 50)
        val modeloError = Validadores.texto(estado.modelo, "el modelo", 50)
        val anioError = Validadores.anio(estado.anio)
        val clienteError = if (estado.clienteId > 0) null else "Selecciona un cliente."
        if (listOf(patenteError, marcaError, modeloError, anioError, clienteError).any { it != null }) {
            _uiState.update {
                it.copy(
                    patenteError = patenteError,
                    marcaError = marcaError,
                    modeloError = modeloError,
                    anioError = anioError,
                    clienteError = clienteError
                )
            }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val resultado = if (estado.editandoId == null) {
                vehiculoRepositorio.crear(
                    estado.patente, estado.marca, estado.modelo, estado.anio.toInt(),
                    estado.vin, estado.tipo, estado.clienteId
                )
            } else {
                vehiculoRepositorio.actualizar(
                    estado.editandoId!!, estado.patente, estado.marca, estado.modelo,
                    estado.anio.toInt(), estado.vin, estado.tipo, estado.clienteId
                )
            }
            when (resultado) {
                is ApiResult.Ok -> {
                    nuevo()
                    _uiState.update { it.copy(guardando = false, mensaje = "Vehículo guardado.") }
                    cargar()
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(guardando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun limpiarMensajes() = _uiState.update { it.copy(error = null, mensaje = null) }
}
