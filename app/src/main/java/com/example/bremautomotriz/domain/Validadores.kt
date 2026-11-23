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
        valor.length < 8 -> "La contraseña debe tener al menos 8 caracteres."
        else -> null
    }

    fun texto(valor: String, campo: String, maximo: Int = 100): String? = when {
        valor.isBlank() -> "Ingresa $campo."
        valor.length > maximo -> "Máximo $maximo caracteres."
        else -> null
    }

    fun patente(valor: String): String? = when {
        valor.isBlank() -> "Ingresa la patente."
        !PATENTE.matches(valor.trim().uppercase()) -> "La patente debe tener 6 caracteres entre A-Z y 0-9."
        else -> null
    }

    fun anio(valor: String): String? {
        val numero = valor.toIntOrNull() ?: return "Ingresa un año válido."
        return if (numero in 1950..2100) null else "El año debe estar entre 1950 y 2100."
    }

    fun kilometraje(valor: String): String? {
        val numero = valor.toIntOrNull() ?: return "Ingresa un número válido."
        return if (numero in 0..1_999_999) null else "El kilometraje debe estar entre 0 y 1999999."
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
        val numero = valor.toIntOrNull() ?: return "Ingresa un monto válido."
        return if (numero > 0) null else "El monto debe ser mayor a 0."
    }

    fun factor(valor: String): String? {
        if (valor.isBlank()) return null
        val numero = valor.toDoubleOrNull() ?: return "Ingresa un factor válido."
        return if (numero >= 1.0) null else "El factor debe ser mayor o igual a 1.00."
    }

    fun ruta(valor: String): String? = when {
        valor.isBlank() -> "Ingresa una ruta o URL."
        valor.length > 255 -> "Máximo 255 caracteres."
        else -> null
    }

    fun texto(valor: String, campo: String): String? = when {
        valor.isBlank() -> "Ingresa $campo."
        valor.length > 50 -> "Máximo 50 caracteres."
        else -> null
    }

    fun anio(valor: String): String? {
        if (valor.isBlank()) return "Ingresa el año."
        val numero = valor.toIntOrNull() ?: return "Ingresa un número válido."
        val maximo = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + 1
        return if (numero < 1950 || numero > maximo) "El año debe estar entre 1950 y $maximo." else null
    }
}
