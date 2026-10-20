package com.example.bremautomotriz.model

data class NuevaOtModel(
    val patente: String = "",
    val kilometraje: String = "",
    val observacion: String = "",
    val vehiculoInfo: String? = null,
    val patenteError: String? = null,
    val kilometrajeError: String? = null,
    val observacionError: String? = null,
    val cargando: Boolean = false
)