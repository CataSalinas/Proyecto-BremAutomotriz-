package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.OrdenRepository
import com.example.bremautomotriz.model.VistaOrden
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BuscarUiState(
    val texto: String = "",
    val resultados: List<VistaOrden> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null
)

class BuscarViewModel(private val repositorio: OrdenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BuscarUiState())
    val uiState: StateFlow<BuscarUiState> = _uiState.asStateFlow()

    private var tarea: Job? = null

    init {
        buscar("")
    }

    fun onTextoChanged(valor: String) {
        _uiState.update { it.copy(texto = valor) }
        tarea?.cancel()
        tarea = viewModelScope.launch {
            delay(300)
            buscar(valor.trim())
        }
    }

    fun reintentar() = buscar(_uiState.value.texto.trim())

    private fun buscar(q: String) {
        tarea?.cancel()
        tarea = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.buscar(q)) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(resultados = resultado.valor, cargando = false)
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}
