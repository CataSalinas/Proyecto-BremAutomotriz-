package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.BremApi
import com.example.bremautomotriz.data.remote.dto.ClienteRequest
import com.example.bremautomotriz.data.remote.dto.VehiculoRequest
import com.example.bremautomotriz.data.remote.ejecutarLlamada
import com.example.bremautomotriz.model.Cliente
import com.example.bremautomotriz.model.Vehiculo

interface ClienteRepository {
    suspend fun listar(): ApiResult<List<Cliente>>
    suspend fun crear(nombre: String, run: String?, telefono: String?, correo: String?): ApiResult<Cliente>
    suspend fun actualizar(id: Long, nombre: String, run: String?, telefono: String?, correo: String?): ApiResult<Cliente>
}

class ClienteRepositoryImpl(private val api: BremApi) : ClienteRepository {

    override suspend fun listar(): ApiResult<List<Cliente>> =
        ejecutarLlamada { api.listarClientes().map { it.aModelo() } }

    override suspend fun crear(
        nombre: String,
        run: String?,
        telefono: String?,
        correo: String?
    ): ApiResult<Cliente> = ejecutarLlamada {
        api.crearCliente(request(nombre, run, telefono, correo)).aModelo()
    }

    override suspend fun actualizar(
        id: Long,
        nombre: String,
        run: String?,
        telefono: String?,
        correo: String?
    ): ApiResult<Cliente> = ejecutarLlamada {
        api.actualizarCliente(id, request(nombre, run, telefono, correo)).aModelo()
    }

    private fun request(nombre: String, run: String?, telefono: String?, correo: String?) =
        ClienteRequest(
            nombre = nombre.trim(),
            run = run?.trim()?.ifBlank { null },
            telefono = telefono?.trim()?.ifBlank { null },
            correo = correo?.trim()?.ifBlank { null }
        )
}

interface VehiculoRepository {
    suspend fun listar(): ApiResult<List<Vehiculo>>
    suspend fun porPatente(patente: String): ApiResult<Vehiculo>
    suspend fun crear(
        patente: String,
        marca: String,
        modelo: String,
        anio: Int,
        vin: String?,
        tipo: String?,
        clienteId: Long
    ): ApiResult<Vehiculo>

    suspend fun actualizar(
        id: Long,
        patente: String,
        marca: String,
        modelo: String,
        anio: Int,
        vin: String?,
        tipo: String?,
        clienteId: Long
    ): ApiResult<Vehiculo>
}

class VehiculoRepositoryImpl(private val api: BremApi) : VehiculoRepository {

    override suspend fun listar(): ApiResult<List<Vehiculo>> =
        ejecutarLlamada { api.listarVehiculos().map { it.aModelo() } }

    override suspend fun porPatente(patente: String): ApiResult<Vehiculo> =
        ejecutarLlamada { api.vehiculoPorPatente(patente.trim().uppercase()).aModelo() }

    override suspend fun crear(
        patente: String,
        marca: String,
        modelo: String,
        anio: Int,
        vin: String?,
        tipo: String?,
        clienteId: Long
    ): ApiResult<Vehiculo> = ejecutarLlamada {
        api.crearVehiculo(request(patente, marca, modelo, anio, vin, tipo, clienteId)).aModelo()
    }

    override suspend fun actualizar(
        id: Long,
        patente: String,
        marca: String,
        modelo: String,
        anio: Int,
        vin: String?,
        tipo: String?,
        clienteId: Long
    ): ApiResult<Vehiculo> = ejecutarLlamada {
        api.actualizarVehiculo(id, request(patente, marca, modelo, anio, vin, tipo, clienteId)).aModelo()
    }

    private fun request(
        patente: String,
        marca: String,
        modelo: String,
        anio: Int,
        vin: String?,
        tipo: String?,
        clienteId: Long
    ) = VehiculoRequest(
        patente = patente.trim().uppercase(),
        marca = marca.trim(),
        modelo = modelo.trim(),
        anio = anio,
        vin = vin?.trim()?.ifBlank { null },
        tipo = tipo?.trim()?.ifBlank { null },
        clienteId = clienteId
    )
}
