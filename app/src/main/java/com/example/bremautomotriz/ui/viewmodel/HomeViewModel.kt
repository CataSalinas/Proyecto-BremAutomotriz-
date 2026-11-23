package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.SessionStore
import com.example.bremautomotriz.data.repository.AuthRepository
import com.example.bremautomotriz.data.repository.IndicadorRepository
import com.example.bremautomotriz.model.Indicadores
import com.example.bremautomotriz.model.UsuarioSesion
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val indicadores: Indicadores? = null,
    val cargando: Boolean = false,
    val error: String? = null
)

class HomeViewModel(
    private val indicadorRepositorio: IndicadorRepository,
    sessionStore: SessionStore,
    private val authRepositorio: AuthRepository
) : ViewModel() {

    val usuario: StateFlow<UsuarioSesion?> = sessionStore.sesion
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun cargarIndicadores() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = indicadorRepositorio.consultar()) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(indicadores = resultado.valor, cargando = false)
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            authRepositorio.cerrarSesion()
            onListo()
        }
    }
}
