package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.JwtDecoder
import com.example.bremautomotriz.data.remote.dto.ClienteResponse
import com.example.bremautomotriz.data.remote.dto.EvidenciaResponse
import com.example.bremautomotriz.data.remote.dto.HistorialResponse
import com.example.bremautomotriz.data.remote.dto.LineaResponse
import com.example.bremautomotriz.data.remote.dto.LoginResponse
import com.example.bremautomotriz.data.remote.dto.MindicadorResponse
import com.example.bremautomotriz.data.remote.dto.OrdenDetalleResponse
import com.example.bremautomotriz.data.remote.dto.OrdenResponse
import com.example.bremautomotriz.data.remote.dto.VehiculoDetalleResponse
import com.example.bremautomotriz.data.remote.dto.VehiculoResponse
import com.example.bremautomotriz.data.remote.dto.VistaOrdenResponse
import com.example.bremautomotriz.model.Cliente
import com.example.bremautomotriz.model.DetalleOrden
import com.example.bremautomotriz.model.Evidencia
import com.example.bremautomotriz.model.Historial
import com.example.bremautomotriz.model.Indicador
import com.example.bremautomotriz.model.Linea
import com.example.bremautomotriz.model.Orden
import com.example.bremautomotriz.model.UsuarioSesion
import com.example.bremautomotriz.model.Vehiculo
import com.example.bremautomotriz.model.VistaOrden

fun LoginResponse.aModelo(): UsuarioSesion = UsuarioSesion(
    userId = JwtDecoder.userId(token.orEmpty()),
    nombre = nombre.orEmpty(),
    email = email.orEmpty(),
    rol = rol.orEmpty(),
    token = token.orEmpty()
)

fun ClienteResponse.aModelo() = Cliente(
    id = id ?: 0,
    nombre = nombre.orEmpty(),
    run = run,
    telefono = telefono,
    correo = correo
)

fun VehiculoResponse.aModelo() = Vehiculo(
    id = id ?: 0,
    patente = patente.orEmpty(),
    marca = marca.orEmpty(),
    modelo = modelo.orEmpty(),
    anio = anio ?: 0,
    vin = vin,
    tipo = tipo,
    clienteId = clienteId ?: 0
)

fun VehiculoDetalleResponse.aModelo() = Vehiculo(
    id = id ?: 0,
    patente = patente.orEmpty(),
    marca = marca.orEmpty(),
    modelo = modelo.orEmpty(),
    anio = anio ?: 0,
    vin = vin,
    tipo = tipo,
    clienteId = clienteId ?: 0,
    clienteNombre = clienteNombre
)

fun VistaOrdenResponse.aModelo() = VistaOrden(
    otId = otId ?: 0,
    patente = patente.orEmpty(),
    nombreCliente = nombreCliente.orEmpty(),
    estado = estado.orEmpty(),
    fechaIngreso = fechaIngreso.orEmpty(),
    total = total ?: 0,
    vehiculoId = vehiculoId ?: 0
)

fun OrdenResponse.aModelo() = Orden(
    id = id ?: 0,
    vehiculoId = vehiculoId ?: 0,
    patente = patente.orEmpty(),
    nombreCliente = nombreCliente.orEmpty(),
    creadoPorId = creadoPorId,
    fechaIngreso = fechaIngreso.orEmpty(),
    fechaRegistro = fechaRegistro,
    kilometraje = kilometraje ?: 0,
    observacionIngreso = observacionIngreso.orEmpty(),
    observacionEntrega = observacionEntrega,
    estado = estado.orEmpty(),
    neto = neto ?: 0,
    iva = iva ?: 0,
    porcentajeIva = porcentajeIva?.toDouble(),
    total = total ?: 0
)

fun LineaResponse.aModelo() = Linea(
    id = id ?: 0,
    otId = otId ?: 0,
    tipo = tipo.orEmpty(),
    descripcion = descripcion.orEmpty(),
    costo = costo ?: 0,
    factor = factor?.toDouble() ?: 1.0,
    cobrado = cobrado ?: 0
)

fun EvidenciaResponse.aModelo() = Evidencia(
    id = id ?: 0,
    otId = otId ?: 0,
    tipo = tipo.orEmpty(),
    ruta = ruta.orEmpty(),
    fecha = fecha.orEmpty()
)

fun HistorialResponse.aModelo() = Historial(
    id = id ?: 0,
    otId = otId ?: 0,
    estadoAnterior = estadoAnterior.orEmpty(),
    estadoNuevo = estadoNuevo.orEmpty(),
    fechaCambio = fechaCambio.orEmpty(),
    observacion = observacion
)

fun OrdenDetalleResponse.aModelo() = DetalleOrden(
    orden = requireNotNull(orden) { "El servidor no devolvió la orden." }.aModelo(),
    lineas = lineas.orEmpty().map { it.aModelo() },
    evidencias = evidencias.orEmpty().map { it.aModelo() },
    historial = historial.orEmpty().map { it.aModelo() }
)

fun MindicadorResponse.aModelo() = Indicador(
    codigo = codigo.orEmpty(),
    nombre = nombre.orEmpty(),
    unidadMedida = unidadMedida.orEmpty(),
    valor = serie?.firstOrNull()?.valor ?: 0.0,
    fecha = serie?.firstOrNull()?.fecha.orEmpty()
)
