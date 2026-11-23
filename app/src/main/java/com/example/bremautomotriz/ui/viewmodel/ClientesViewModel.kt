package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.ClienteRepository
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.Cliente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ClientesUiState(
    val clientes: List<Cliente> = emptyList(),
    val nombre: String = "",
    val run: String = "",
    val telefono: String = "",
    val correo: String = "",
    val nombreError: String? = null,
    val editandoId: Long? = null,
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null
)

class ClientesViewModel(private val repositorio: ClienteRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.listar()) {
                is ApiResult.Ok -> _uiState.update { it.copy(clientes = resultado.valor, cargando = false) }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun onNombreChanged(valor: String) = _uiState.update { it.copy(nombre = valor, nombreError = null) }
    fun onRunChanged(valor: String) = _uiState.update { it.copy(run = valor.trim().take(12)) }
    fun onTelefonoChanged(valor: String) = _uiState.update { it.copy(telefono = valor.trim().take(20)) }
    fun onCorreoChanged(valor: String) = _uiState.update { it.copy(correo = valor.trim().take(100)) }

    fun editar(cliente: Cliente) = _uiState.update {
        it.copy(
            editandoId = cliente.id,
            nombre = cliente.nombre,
            run = cliente.run.orEmpty(),
            telefono = cliente.telefono.orEmpty(),
            correo = cliente.correo.orEmpty(),
            nombreError = null,
            error = null,
            mensaje = null
        )
    }

    fun nuevo() = _uiState.update {
        it.copy(
            editandoId = null,
            nombre = "",
            run = "",
            telefono = "",
            correo = "",
            nombreError = null,
            error = null,
            mensaje = null
        )
    }

    fun guardar() {
        val estado = _uiState.value
        val error = Validadores.texto(estado.nombre, "el nombre")
        if (error != null) {
            _uiState.update { it.copy(nombreError = error) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val resultado = if (estado.editandoId == null) {
                repositorio.crear(estado.nombre, estado.run, estado.telefono, estado.correo)
            } else {
                repositorio.actualizar(estado.editandoId!!, estado.nombre, estado.run, estado.telefono, estado.correo)
            }
            when (resultado) {
                is ApiResult.Ok -> {
                    nuevo()
                    _uiState.update { it.copy(guardando = false, mensaje = "Cliente guardado.") }
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
