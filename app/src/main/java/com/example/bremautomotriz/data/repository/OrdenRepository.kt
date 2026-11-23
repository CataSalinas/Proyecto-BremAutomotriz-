package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.BremApi
import com.example.bremautomotriz.data.remote.dto.CambioEstadoRequest
import com.example.bremautomotriz.data.remote.dto.EvidenciaRequest
import com.example.bremautomotriz.data.remote.dto.LineaRequest
import com.example.bremautomotriz.data.remote.dto.ObservacionRequest
import com.example.bremautomotriz.data.remote.dto.OrdenRequest
import com.example.bremautomotriz.data.remote.ejecutarLlamada
import com.example.bremautomotriz.model.DetalleOrden
import com.example.bremautomotriz.model.Evidencia
import com.example.bremautomotriz.model.Linea
import com.example.bremautomotriz.model.Orden
import com.example.bremautomotriz.model.VistaOrden
import java.math.BigDecimal

interface OrdenRepository {
    suspend fun buscar(q: String): ApiResult<List<VistaOrden>>
    suspend fun detalle(id: Long): ApiResult<DetalleOrden>
    suspend fun crear(vehiculoId: Long, kilometraje: Int, observacion: String): ApiResult<Long>
    suspend fun actualizarObservacion(id: Long, observacion: String): ApiResult<Orden>
    suspend fun cambiarEstado(id: Long, estado: String, observacionEntrega: String?): ApiResult<DetalleOrden>
    suspend fun crearLinea(ordenId: Long, tipo: String, descripcion: String, costo: Int, factor: Double?): ApiResult<Linea>
    suspend fun actualizarLinea(ordenId: Long, lineaId: Long, tipo: String, descripcion: String, costo: Int, factor: Double?): ApiResult<Linea>
    suspend fun eliminarLinea(ordenId: Long, lineaId: Long): ApiResult<Unit>
    suspend fun crearEvidencia(ordenId: Long, ruta: String, tipo: String): ApiResult<Evidencia>
    suspend fun historial(ordenId: Long): ApiResult<List<com.example.bremautomotriz.model.Historial>>
}

class OrdenRepositoryImpl(private val api: BremApi) : OrdenRepository {

    override suspend fun buscar(q: String): ApiResult<List<VistaOrden>> =
        ejecutarLlamada { api.buscarOrdenes(q).map { it.aModelo() } }

    override suspend fun detalle(id: Long): ApiResult<DetalleOrden> =
        ejecutarLlamada { api.detalleOrden(id).aModelo() }

    override suspend fun crear(vehiculoId: Long, kilometraje: Int, observacion: String): ApiResult<Long> =
        ejecutarLlamada {
            val respuesta = api.crearOrden(
                OrdenRequest(vehiculoId, kilometraje, observacion.trim())
            )
            requireNotNull(respuesta.orden?.id) { "El servidor no devolvió el id de la orden." }
        }

    override suspend fun actualizarObservacion(id: Long, observacion: String): ApiResult<Orden> =
        ejecutarLlamada { api.actualizarObservacion(id, ObservacionRequest(observacion.trim())).aModelo() }

    override suspend fun cambiarEstado(
        id: Long,
        estado: String,
        observacionEntrega: String?
    ): ApiResult<DetalleOrden> = ejecutarLlamada {
        api.cambiarEstado(id, CambioEstadoRequest(estado, observacionEntrega?.trim()?.takeIf { it.isNotBlank() })).aModelo()
    }

    override suspend fun crearLinea(
        ordenId: Long,
        tipo: String,
        descripcion: String,
        costo: Int,
        factor: Double?
    ): ApiResult<Linea> = ejecutarLlamada {
        api.crearLinea(ordenId, lineaRequest(tipo, descripcion, costo, factor)).aModelo()
    }

    override suspend fun actualizarLinea(
        ordenId: Long,
        lineaId: Long,
        tipo: String,
        descripcion: String,
        costo: Int,
        factor: Double?
    ): ApiResult<Linea> = ejecutarLlamada {
        api.actualizarLinea(ordenId, lineaId, lineaRequest(tipo, descripcion, costo, factor)).aModelo()
    }

    override suspend fun eliminarLinea(ordenId: Long, lineaId: Long): ApiResult<Unit> =
        ejecutarLlamada {
            api.eliminarLinea(ordenId, lineaId)
            Unit
        }

    override suspend fun crearEvidencia(ordenId: Long, ruta: String, tipo: String): ApiResult<Evidencia> =
        ejecutarLlamada { api.crearEvidencia(ordenId, EvidenciaRequest(ruta, tipo)).aModelo() }

    override suspend fun historial(ordenId: Long) =
        ejecutarLlamada { api.historial(ordenId).map { it.aModelo() } }

    private fun lineaRequest(tipo: String, descripcion: String, costo: Int, factor: Double?) =
        LineaRequest(
            tipo = tipo,
            descripcion = descripcion.trim(),
            costo = costo,
            factor = factor?.let { BigDecimal.valueOf(it) }
        )
}
