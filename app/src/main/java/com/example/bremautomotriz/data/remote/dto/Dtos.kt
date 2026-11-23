package com.example.bremautomotriz.data.remote.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class LoginRequest(val email: String, val password: String)

data class LoginResponse(
    val token: String? = null,
    val nombre: String? = null,
    val email: String? = null,
    val rol: String? = null
)

data class ClienteRequest(
    val nombre: String,
    val run: String? = null,
    val telefono: String? = null,
    val correo: String? = null
)

data class ClienteResponse(
    val id: Long? = null,
    val nombre: String? = null,
    val run: String? = null,
    val telefono: String? = null,
    val correo: String? = null
)

data class VehiculoRequest(
    val patente: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val vin: String? = null,
    val tipo: String? = null,
    val clienteId: Long
)

data class VehiculoResponse(
    val id: Long? = null,
    val patente: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val anio: Int? = null,
    val vin: String? = null,
    val tipo: String? = null,
    val clienteId: Long? = null
)

data class VehiculoDetalleResponse(
    val id: Long? = null,
    val patente: String? = null,
    val marca: String? = null,
    val modelo: String? = null,
    val anio: Int? = null,
    val vin: String? = null,
    val tipo: String? = null,
    val clienteId: Long? = null,
    val clienteNombre: String? = null,
    val clienteRun: String? = null,
    val clienteTelefono: String? = null,
    val clienteCorreo: String? = null
)

data class OrdenRequest(
    val vehiculoId: Long,
    val kilometraje: Int,
    val observacionIngreso: String,
    val porcentajeIva: BigDecimal? = null,
    val lineas: List<LineaRequest>? = null
)

data class OrdenResponse(
    val id: Long? = null,
    val vehiculoId: Long? = null,
    val patente: String? = null,
    val nombreCliente: String? = null,
    val creadoPorId: Long? = null,
    val fechaIngreso: String? = null,
    val fechaRegistro: String? = null,
    val kilometraje: Int? = null,
    val observacionIngreso: String? = null,
    val observacionEntrega: String? = null,
    val estado: String? = null,
    val neto: Int? = null,
    val iva: Int? = null,
    val porcentajeIva: BigDecimal? = null,
    val total: Int? = null
)

data class OrdenDetalleResponse(
    val orden: OrdenResponse? = null,
    val lineas: List<LineaResponse>? = null,
    val evidencias: List<EvidenciaResponse>? = null,
    val historial: List<HistorialResponse>? = null
)

data class VistaOrdenResponse(
    val otId: Long? = null,
    val patente: String? = null,
    val nombreCliente: String? = null,
    val estado: String? = null,
    val fechaIngreso: String? = null,
    val total: Int? = null,
    val vehiculoId: Long? = null
)

data class ObservacionRequest(val observacionIngreso: String)

data class CambioEstadoRequest(val estado: String, val observacionEntrega: String? = null)

data class LineaRequest(
    val tipo: String,
    val descripcion: String,
    val costo: Int,
    val factor: BigDecimal? = null
)

data class LineaResponse(
    val id: Long? = null,
    val otId: Long? = null,
    val tipo: String? = null,
    val descripcion: String? = null,
    val costo: Int? = null,
    val factor: BigDecimal? = null,
    val cobrado: Int? = null
)

data class EvidenciaRequest(val ruta: String, val tipo: String? = null)

data class EvidenciaResponse(
    val id: Long? = null,
    val otId: Long? = null,
    val tipo: String? = null,
    val ruta: String? = null,
    val fecha: String? = null
)

data class HistorialResponse(
    val id: Long? = null,
    val otId: Long? = null,
    val estadoAnterior: String? = null,
    val estadoNuevo: String? = null,
    val fechaCambio: String? = null,
    val observacion: String? = null
)

data class ErrorResponse(
    val status: Int? = null,
    val error: String? = null,
    val mensaje: String? = null,
    val timestamp: String? = null
)

data class MindicadorResponse(
    val version: String? = null,
    val autor: String? = null,
    val codigo: String? = null,
    val nombre: String? = null,
    @SerializedName("unidad_medida") val unidadMedida: String? = null,
    val serie: List<SerieItemResponse>? = null
)

data class SerieItemResponse(
    val fecha: String? = null,
    val valor: Double? = null
)
