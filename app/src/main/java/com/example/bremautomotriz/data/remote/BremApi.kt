package com.example.bremautomotriz.data.remote

import com.example.bremautomotriz.data.remote.dto.CambioEstadoRequest
import com.example.bremautomotriz.data.remote.dto.ClienteRequest
import com.example.bremautomotriz.data.remote.dto.ClienteResponse
import com.example.bremautomotriz.data.remote.dto.EvidenciaRequest
import com.example.bremautomotriz.data.remote.dto.EvidenciaResponse
import com.example.bremautomotriz.data.remote.dto.HistorialResponse
import com.example.bremautomotriz.data.remote.dto.LineaRequest
import com.example.bremautomotriz.data.remote.dto.LineaResponse
import com.example.bremautomotriz.data.remote.dto.LoginRequest
import com.example.bremautomotriz.data.remote.dto.LoginResponse
import com.example.bremautomotriz.data.remote.dto.ObservacionRequest
import com.example.bremautomotriz.data.remote.dto.OrdenDetalleResponse
import com.example.bremautomotriz.data.remote.dto.OrdenRequest
import com.example.bremautomotriz.data.remote.dto.OrdenResponse
import com.example.bremautomotriz.data.remote.dto.VehiculoDetalleResponse
import com.example.bremautomotriz.data.remote.dto.VehiculoRequest
import com.example.bremautomotriz.data.remote.dto.VehiculoResponse
import com.example.bremautomotriz.data.remote.dto.VistaOrdenResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BremApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("clientes")
    suspend fun listarClientes(): List<ClienteResponse>

    @POST("clientes")
    suspend fun crearCliente(@Body request: ClienteRequest): ClienteResponse

    @PUT("clientes/{id}")
    suspend fun actualizarCliente(@Path("id") id: Long, @Body request: ClienteRequest): ClienteResponse

    @GET("vehiculos")
    suspend fun listarVehiculos(): List<VehiculoResponse>

    @GET("vehiculos/patente/{patente}")
    suspend fun vehiculoPorPatente(@Path("patente") patente: String): VehiculoDetalleResponse

    @POST("vehiculos")
    suspend fun crearVehiculo(@Body request: VehiculoRequest): VehiculoResponse

    @PUT("vehiculos/{id}")
    suspend fun actualizarVehiculo(@Path("id") id: Long, @Body request: VehiculoRequest): VehiculoResponse

    @GET("ordenes/buscar")
    suspend fun buscarOrdenes(@Query("q") q: String): List<VistaOrdenResponse>

    @GET("ordenes/{id}")
    suspend fun detalleOrden(@Path("id") id: Long): OrdenDetalleResponse

    @POST("ordenes")
    suspend fun crearOrden(@Body request: OrdenRequest): OrdenDetalleResponse

    @PUT("ordenes/{id}")
    suspend fun actualizarObservacion(@Path("id") id: Long, @Body request: ObservacionRequest): OrdenResponse

    @PATCH("ordenes/{id}/estado")
    suspend fun cambiarEstado(@Path("id") id: Long, @Body request: CambioEstadoRequest): OrdenDetalleResponse

    @GET("ordenes/{ordenId}/lineas")
    suspend fun listarLineas(@Path("ordenId") ordenId: Long): List<LineaResponse>

    @POST("ordenes/{ordenId}/lineas")
    suspend fun crearLinea(@Path("ordenId") ordenId: Long, @Body request: LineaRequest): LineaResponse

    @PUT("ordenes/{ordenId}/lineas/{lineaId}")
    suspend fun actualizarLinea(
        @Path("ordenId") ordenId: Long,
        @Path("lineaId") lineaId: Long,
        @Body request: LineaRequest
    ): LineaResponse

    @DELETE("ordenes/{ordenId}/lineas/{lineaId}")
    suspend fun eliminarLinea(@Path("ordenId") ordenId: Long, @Path("lineaId") lineaId: Long)

    @POST("ordenes/{ordenId}/evidencias")
    suspend fun crearEvidencia(@Path("ordenId") ordenId: Long, @Body request: EvidenciaRequest): EvidenciaResponse

    @GET("ordenes/{ordenId}/historial")
    suspend fun historial(@Path("ordenId") ordenId: Long): List<HistorialResponse>
}
