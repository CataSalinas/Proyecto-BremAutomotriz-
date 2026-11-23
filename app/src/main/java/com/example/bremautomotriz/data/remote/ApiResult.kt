package com.example.bremautomotriz.data.remote

import com.example.bremautomotriz.data.remote.dto.ErrorResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

sealed interface ApiResult<out T> {
    data class Ok<out T>(val valor: T) : ApiResult<T>
    data class Fallo(val status: Int? = null, val mensaje: String) : ApiResult<Nothing>
}

object ErrorParser {
    private val gson = Gson()

    fun mensaje(cuerpo: String?, predeterminado: String = "No se pudo completar la solicitud."): String =
        runCatching {
            val error = gson.fromJson(cuerpo, ErrorResponse::class.java)
            error?.mensaje?.takeIf { it.isNotBlank() } ?: predeterminado
        }.getOrDefault(predeterminado)
}

suspend fun <T> ejecutarLlamada(llamada: suspend () -> T): ApiResult<T> = try {
    ApiResult.Ok(llamada())
} catch (e: HttpException) {
    val cuerpo = e.response()?.errorBody()?.string()
    ApiResult.Fallo(e.code(), ErrorParser.mensaje(cuerpo, "Error ${e.code()} en el servidor."))
} catch (e: IOException) {
    ApiResult.Fallo(null, "Sin conexión con el servidor.")
} catch (e: Exception) {
    ApiResult.Fallo(null, e.message?.takeIf { it.isNotBlank() } ?: "Error inesperado.")
}
