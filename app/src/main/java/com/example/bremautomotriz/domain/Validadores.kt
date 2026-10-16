package com.example.bremautomotriz.domain

object Validadores {
    private val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    private val PATENTE = Regex("^[A-Z0-9]{6}$")

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

    fun patente(valor: String): String? = when {
        valor.isBlank() -> "Ingresa la patente."
        !PATENTE.matches(valor.trim().uppercase()) -> "La patente debe tener 6 letras o números."
        else -> null
    }

    fun kilometraje(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el kilometraje."
        val numero = valor.toIntOrNull() ?: return "Ingresa un número válido."
        return if (numero > 1_999_999) "El kilometraje es demasiado alto." else null
    }

    fun observacion(valor: String): String? = when {
        valor.isBlank() -> "Describe el motivo de ingreso."
        valor.length > 500 -> "Máximo 500 caracteres."
        else -> null
    }

    fun descripcionLinea(valor: String): String? = when {
        valor.isBlank() -> "Ingresa una descripción."
        valor.length > 100 -> "Máximo 100 caracteres."
        else -> null
    }

    fun monto(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el monto."
        val numero = valor.toIntOrNull() ?: return "Ingresa un número válido."
        return if (numero <= 0) "El monto debe ser mayor a 0." else null
    }
}