package com.example.bremautomotriz.model

data class UsuarioSesion(
    val userId: Long = 0,
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val token: String = ""
)

data class Cliente(
    val id: Long = 0,
    val nombre: String = "",
    val run: String? = null,
    val telefono: String? = null,
    val correo: String? = null
)

data class Vehiculo(
    val id: Long = 0,
    val patente: String = "",
    val marca: String = "",
    val modelo: String = "",
    val anio: Int = 0,
    val vin: String? = null,
    val tipo: String? = null,
    val clienteId: Long = 0,
    val clienteNombre: String? = null
)

data class VistaOrden(
    val otId: Long = 0,
    val patente: String = "",
    val nombreCliente: String = "",
    val estado: String = "",
    val fechaIngreso: String = "",
    val total: Int = 0,
    val vehiculoId: Long = 0
)

data class Orden(
    val id: Long = 0,
    val vehiculoId: Long = 0,
    val patente: String = "",
    val nombreCliente: String = "",
    val creadoPorId: Long? = null,
    val fechaIngreso: String = "",
    val fechaRegistro: String? = null,
    val kilometraje: Int = 0,
    val observacionIngreso: String = "",
    val observacionEntrega: String? = null,
    val estado: String = "",
    val neto: Int = 0,
    val iva: Int = 0,
    val porcentajeIva: Double? = null,
    val total: Int = 0
)

data class Linea(
    val id: Long = 0,
    val otId: Long = 0,
    val tipo: String = "",
    val descripcion: String = "",
    val costo: Int = 0,
    val factor: Double = 1.0,
    val cobrado: Int = 0
)

data class Evidencia(
    val id: Long = 0,
    val otId: Long = 0,
    val tipo: String = "",
    val ruta: String = "",
    val fecha: String = ""
)

data class Historial(
    val id: Long = 0,
    val otId: Long = 0,
    val estadoAnterior: String = "",
    val estadoNuevo: String = "",
    val fechaCambio: String = "",
    val observacion: String? = null
)

data class DetalleOrden(
    val orden: Orden,
    val lineas: List<Linea> = emptyList(),
    val evidencias: List<Evidencia> = emptyList(),
    val historial: List<Historial> = emptyList()
)

data class Indicador(
    val codigo: String = "",
    val nombre: String = "",
    val unidadMedida: String = "",
    val valor: Double = 0.0,
    val fecha: String = ""
)

data class Indicadores(
    val uf: Indicador = Indicador(),
    val dolar: Indicador = Indicador()
)
