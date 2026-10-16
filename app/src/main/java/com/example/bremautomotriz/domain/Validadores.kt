package com.example.bremautomotriz.domain

object Validadores {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    fun email(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo."
        !EMAIL.matches(valor.trim()) -> "El correo no es válido."
        else -> null
    }

    fun password(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu contraseña."
        valor.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }
}