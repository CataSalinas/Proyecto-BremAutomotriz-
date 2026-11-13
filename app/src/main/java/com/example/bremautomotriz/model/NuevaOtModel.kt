package com.example.bremautomotriz.model

data class NuevaOtModel(
    val patente: String = "",
    val kilometraje: String = "",
    val observacion: String = "",
    val vehiculoInfo: String? = null,
    val vehiculoNuevo: Boolean = false,
    val marca: String = "",
    val modelo: String = "",
    val anio: String = "",
    val nombreCliente: String = "",
    val run: String = "",
    val patenteError: String? = null,
    val kilometrajeError: String? = null,
    val observacionError: String? = null,
    val marcaError: String? = null,
    val modeloError: String? = null,
    val anioError: String? = null,
    val nombreClienteError: String? = null,
    val cargando: Boolean = false
)