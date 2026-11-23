package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.BremApi
import com.example.bremautomotriz.data.remote.SessionStore
import com.example.bremautomotriz.data.remote.dto.LoginRequest
import com.example.bremautomotriz.data.remote.ejecutarLlamada
import com.example.bremautomotriz.model.UsuarioSesion

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResult<UsuarioSesion>
    suspend fun cerrarSesion()
}

class AuthRepositoryImpl(
    private val api: BremApi,
    private val sessionStore: SessionStore
) : AuthRepository {

    override suspend fun login(email: String, password: String): ApiResult<UsuarioSesion> =
        when (val resultado = ejecutarLlamada { api.login(LoginRequest(email.trim(), password)) }) {
            is ApiResult.Ok -> {
                val usuario = resultado.valor.aModelo()
                sessionStore.guardar(usuario)
                ApiResult.Ok(usuario)
            }
            is ApiResult.Fallo -> resultado
        }

    override suspend fun cerrarSesion() = sessionStore.limpiar()
}
