package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.data.DatosEjemplo
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.data.repository.UsuarioRepository
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.LoginModel
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val repositorio = UsuarioRepository(db.usuarioDao())
    private val carga = viewModelScope.launch { DatosEjemplo.cargarSiVacio(db) }

    var uiState by mutableStateOf(LoginModel())
        private set

    fun onEmailChanged(email: String) {
        uiState = uiState.copy(email = email, emailError = null, errorGeneral = null)
    }

    fun onPasswordChanged(password: String) {
        uiState = uiState.copy(password = password, passwordError = null, errorGeneral = null)
    }

    fun onLoginClick(onExito: () -> Unit) {
        val emailError = Validadores.email(uiState.email)
        val passwordError = Validadores.password(uiState.password)

        if (emailError != null || passwordError != null) {
            uiState = uiState.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        viewModelScope.launch {
            uiState = uiState.copy(cargando = true, errorGeneral = null)
            carga.join()
            val usuario = repositorio.iniciarSesion(uiState.email, uiState.password)
            if (usuario == null) {
                uiState = uiState.copy(
                    cargando = false,
                    errorGeneral = "Correo o contraseña incorrectos."
                )
            } else {
                Sesion.usuario = usuario
                uiState = uiState.copy(cargando = false)
                onExito()
            }
        }
    }
}