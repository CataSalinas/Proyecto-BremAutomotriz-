package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.AuthRepository
import com.example.bremautomotriz.domain.Validadores
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val error: String? = null,
    val cargando: Boolean = false
)

class LoginViewModel(private val repositorio: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(valor: String) = _uiState.update {
        it.copy(email = valor, emailError = null, error = null)
    }

    fun onPasswordChanged(valor: String) = _uiState.update {
        it.copy(password = valor, passwordError = null, error = null)
    }

    fun iniciarSesion(onExito: () -> Unit) {
        val estado = _uiState.value
        val emailError = Validadores.email(estado.email)
        val passwordError = Validadores.password(estado.password)
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.login(estado.email, estado.password)) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(cargando = false) }
                    onExito()
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }
}
