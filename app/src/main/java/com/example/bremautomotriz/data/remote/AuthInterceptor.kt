package com.example.bremautomotriz.data.remote

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.Interceptor
import okhttp3.Response

class TokenHolder {
    @Volatile
    var token: String? = null
}

class AuthEvents {
    private val _expirado = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expirado: SharedFlow<Unit> = _expirado

    fun notificar() {
        _expirado.tryEmit(Unit)
    }
}

class AuthInterceptor(
    private val tokenHolder: TokenHolder,
    private val eventos: AuthEvents
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenHolder.token
        val solicitud = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val respuesta = chain.proceed(solicitud)
        val esLogin = solicitud.url.encodedPath.endsWith("/auth/login")
        if (respuesta.code == 401 && !esLogin) {
            tokenHolder.token = null
            eventos.notificar()
        }
        return respuesta
    }
}
