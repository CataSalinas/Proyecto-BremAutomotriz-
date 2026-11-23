package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.BremApi
import com.example.bremautomotriz.data.remote.MindicadorApi
import com.example.bremautomotriz.data.remote.SessionStore
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
import com.example.bremautomotriz.model.UsuarioSesion
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.math.BigDecimal
import java.util.Base64

class RepositoryTest {

    private val api = mockk<BremApi>()
    private val apiExterna = mockk<MindicadorApi>()
    private val sessionStore = mockk<SessionStore>()

    @Test
    fun `login guarda sesion y decodifica userId`() = runBlocking {
        coEvery { sessionStore.guardar(any()) } returns Unit
        coEvery { api.login(any()) } returns LoginResponse(
            token = jwt(42),
            nombre = "Jefe",
            email = "jefe@brem.cl",
            rol = "JEFE"
        )
        val repositorio = AuthRepositoryImpl(api, sessionStore)

        val resultado = repositorio.login(" jefe@brem.cl ", "jefe1234")

        assertTrue(resultado is ApiResult.Ok)
        val usuario = (resultado as ApiResult.Ok).valor
        assertEquals(42L, usuario.userId)
        assertEquals("JEFE", usuario.rol)
        coVerify { api.login(match { it.email == "jefe@brem.cl" }) }
        coVerify { sessionStore.guardar(match { it.token == jwt(42) }) }

        coEvery { sessionStore.limpiar() } returns Unit
        repositorio.cerrarSesion()
        coVerify { sessionStore.limpiar() }
    }

    @Test
    fun `login propaga error del servidor`() = runBlocking {
        val cuerpo = """{"status":401,"error":"No autorizado","mensaje":"Credenciales inválidas"}"""
        val respuesta = Response.error<Any>(
            401,
            cuerpo.toResponseBody("application/json".toMediaTypeOrNull())
        )
        coEvery { api.login(any()) } throws HttpException(respuesta)
        val repositorio = AuthRepositoryImpl(api, sessionStore)

        val resultado = repositorio.login("jefe@brem.cl", "incorrecta")

        assertTrue(resultado is ApiResult.Fallo)
        assertEquals(401, (resultado as ApiResult.Fallo).status)
        assertEquals("Credenciales inválidas", resultado.mensaje)
        coVerify(exactly = 0) { sessionStore.guardar(any()) }
    }

    @Test
    fun `repositorio de ordenes ejecuta todas las operaciones`() = runBlocking {
        val detalle = detalleResponse()
        coEvery { api.buscarOrdenes("ABC") } returns listOf(vistaResponse())
        coEvery { api.detalleOrden(1) } returns detalle
        coEvery { api.crearOrden(any()) } returns detalle
        coEvery { api.actualizarObservacion(1, any()) } returns ordenResponse()
        coEvery { api.cambiarEstado(1, any()) } returns detalle
        coEvery { api.crearLinea(1, any()) } returns lineaResponse()
        coEvery { api.actualizarLinea(1, 4, any()) } returns lineaResponse()
        coEvery { api.eliminarLinea(1, 4) } returns Unit
        coEvery { api.crearEvidencia(1, any()) } returns evidenciaResponse()
        coEvery { api.historial(1) } returns listOf(historialResponse())
        val repositorio = OrdenRepositoryImpl(api)

        val busqueda = repositorio.buscar("ABC")
        assertTrue(busqueda is ApiResult.Ok)
        assertEquals("ABC123", (busqueda as ApiResult.Ok).valor.first().patente)

        val detalleResultado = repositorio.detalle(1)
        assertTrue(detalleResultado is ApiResult.Ok)
        val modelo = (detalleResultado as ApiResult.Ok).valor
        assertEquals(1L, modelo.orden.id)
        assertEquals(1, modelo.lineas.size)
        assertEquals(1, modelo.evidencias.size)
        assertEquals(1, modelo.historial.size)
        assertEquals(11900, modelo.orden.total)

        val creado = repositorio.crear(2, 100, " Frenos ")
        assertTrue(creado is ApiResult.Ok)
        assertEquals(1L, (creado as ApiResult.Ok).valor)
        coVerify { api.crearOrden(match { it.observacionIngreso == "Frenos" }) }

        val observacion = repositorio.actualizarObservacion(1, " Frenos ")
        assertTrue(observacion is ApiResult.Ok)
        coVerify { api.actualizarObservacion(1, match { it.observacionIngreso == "Frenos" }) }

        val cambio = repositorio.cambiarEstado(1, "ENTREGADO", "  Entregado  ")
        assertTrue(cambio is ApiResult.Ok)
        coVerify {
            api.cambiarEstado(1, match {
                it.estado == "ENTREGADO" && it.observacionEntrega == "Entregado"
            })
        }

        val linea = repositorio.crearLinea(1, "REPUESTO", " Pastillas ", 10000, 1.3)
        assertTrue(linea is ApiResult.Ok)
        assertEquals(4L, (linea as ApiResult.Ok).valor.id)
        coVerify {
            api.crearLinea(1, match {
                it.descripcion == "Pastillas" && it.factor == BigDecimal.valueOf(1.3)
            })
        }

        assertTrue(repositorio.actualizarLinea(1, 4, "OM", "Mano de obra", 5000, null) is ApiResult.Ok)
        coVerify { api.actualizarLinea(1, 4, match { it.factor == null }) }

        assertTrue(repositorio.eliminarLinea(1, 4) is ApiResult.Ok)
        coVerify { api.eliminarLinea(1, 4) }

        val evidencia = repositorio.crearEvidencia(1, "/tmp/foto.jpg", "VEHICULO")
        assertTrue(evidencia is ApiResult.Ok)
        assertEquals("/tmp/foto.jpg", (evidencia as ApiResult.Ok).valor.ruta)

        val historial = repositorio.historial(1)
        assertTrue(historial is ApiResult.Ok)
        assertEquals("PENDIENTE", (historial as ApiResult.Ok).valor.first().estadoAnterior)
    }

    @Test
    fun `repositorio de ordenes normaliza fallos`() = runBlocking {
        coEvery { api.buscarOrdenes("") } throws IOException("sin red")
        val repositorio = OrdenRepositoryImpl(api)

        val resultado = repositorio.buscar("")

        assertTrue(resultado is ApiResult.Fallo)
        assertNull((resultado as ApiResult.Fallo).status)
        assertEquals("Sin conexión con el servidor.", resultado.mensaje)
    }

    @Test
    fun `repositorio de catalogos ejecuta CRUD de clientes y vehiculos`() = runBlocking {
        coEvery { api.listarClientes() } returns listOf(ClienteResponse(id = 9, nombre = "Cliente"))
        coEvery { api.crearCliente(any()) } returns ClienteResponse(id = 10, nombre = "Nuevo")
        coEvery { api.actualizarCliente(9, any()) } returns ClienteResponse(id = 9, nombre = "Editado")
        coEvery { api.listarVehiculos() } returns listOf(VehiculoResponse(id = 8, patente = "ABC123"))
        coEvery { api.vehiculoPorPatente("ABC123") } returns VehiculoDetalleResponse(
            id = 8,
            patente = "ABC123",
            clienteId = 9,
            clienteNombre = "Cliente"
        )
        coEvery { api.crearVehiculo(any()) } returns VehiculoResponse(id = 8, patente = "ABC123")
        coEvery { api.actualizarVehiculo(8, any()) } returns VehiculoResponse(id = 8, patente = "ABC123")
        val clientes = ClienteRepositoryImpl(api)
        val vehiculos = VehiculoRepositoryImpl(api)

        val listaClientes = clientes.listar()
        assertTrue(listaClientes is ApiResult.Ok)
        assertEquals("Cliente", (listaClientes as ApiResult.Ok).valor.first().nombre)

        assertTrue(clientes.crear("Nuevo", " ", " ", " ") is ApiResult.Ok)
        coVerify {
            api.crearCliente(match {
                it.nombre == "Nuevo" && it.run == null && it.telefono == null && it.correo == null
            })
        }

        val clienteEditado = clientes.actualizar(9, " Editado ", null, null, null)
        assertEquals("Editado", (clienteEditado as ApiResult.Ok).valor.nombre)
        coVerify { api.actualizarCliente(9, match { it.nombre == "Editado" }) }

        val listaVehiculos = vehiculos.listar()
        assertEquals("ABC123", (listaVehiculos as ApiResult.Ok).valor.first().patente)

        val porPatente = vehiculos.porPatente(" abc123 ")
        assertEquals("Cliente", (porPatente as ApiResult.Ok).valor.clienteNombre)

        assertTrue(vehiculos.crear("abc123", " Toyota ", " Yaris ", 2024, " ", "", 9) is ApiResult.Ok)
        coVerify {
            api.crearVehiculo(match {
                it.patente == "ABC123" && it.vin == null && it.tipo == null
            })
        }

        assertTrue(vehiculos.actualizar(8, "ABC123", "Toyota", "Yaris", 2025, "VIN", "AUTO", 9) is ApiResult.Ok)
        coVerify { api.actualizarVehiculo(8, match { it.anio == 2025 }) }
    }

    @Test
    fun `repositorio de catalogos normaliza fallo`() = runBlocking {
        coEvery { api.listarVehiculos() } throws IOException("sin red")
        val repositorio = VehiculoRepositoryImpl(api)

        val resultado = repositorio.listar()

        assertTrue(resultado is ApiResult.Fallo)
        assertEquals("Sin conexión con el servidor.", (resultado as ApiResult.Fallo).mensaje)
    }

    @Test
    fun `repositorio de indicadores toma primer valor de la serie`() = runBlocking {
        coEvery { apiExterna.uf() } returns MindicadorResponse(
            codigo = "uf",
            nombre = "Unidad de fomento",
            unidadMedida = "UF",
            serie = listOf(
                com.example.bremautomotriz.data.remote.dto.SerieItemResponse(
                    "2026-11-22T03:00:00.000Z",
                    41384.81
                ),
                com.example.bremautomotriz.data.remote.dto.SerieItemResponse(
                    "2026-11-21T03:00:00.000Z",
                    41300.0
                )
            )
        )
        coEvery { apiExterna.dolar() } returns MindicadorResponse(
            codigo = "dolar",
            nombre = "Dólar observado",
            unidadMedida = "USD",
            serie = emptyList()
        )
        val repositorio = IndicadorRepositoryImpl(apiExterna)

        val resultado = repositorio.consultar()

        assertTrue(resultado is ApiResult.Ok)
        val indicadores = (resultado as ApiResult.Ok).valor
        assertEquals(41384.81, indicadores.uf.valor, 0.0)
        assertEquals("UF", indicadores.uf.unidadMedida)
        assertEquals(0.0, indicadores.dolar.valor, 0.0)
    }

    @Test
    fun `repositorio de indicadores normaliza fallo`() = runBlocking {
        coEvery { apiExterna.uf() } throws IOException("sin red")
        coEvery { apiExterna.dolar() } returns MindicadorResponse()
        val repositorio = IndicadorRepositoryImpl(apiExterna)

        val resultado = repositorio.consultar()

        assertTrue(resultado is ApiResult.Fallo)
        assertEquals("Sin conexión con el servidor.", (resultado as ApiResult.Fallo).mensaje)
    }

    private fun jwt(userId: Long): String {
        val carga = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"userId\":$userId}".toByteArray(Charsets.UTF_8))
        return "eyJhbGciOiJIUzI1NiJ9.$carga.firma"
    }

    private fun ordenResponse() = OrdenResponse(
        id = 1,
        vehiculoId = 2,
        patente = "ABC123",
        nombreCliente = "Cliente",
        creadoPorId = 5,
        fechaIngreso = "2026-11-22T10:30:00",
        fechaRegistro = "2026-11-22T10:31:00",
        kilometraje = 100,
        observacionIngreso = "Frenos",
        estado = "PENDIENTE",
        neto = 10000,
        iva = 1900,
        porcentajeIva = BigDecimal("19.00"),
        total = 11900
    )

    private fun detalleResponse() = OrdenDetalleResponse(
        orden = ordenResponse(),
        lineas = listOf(lineaResponse()),
        evidencias = listOf(evidenciaResponse()),
        historial = listOf(historialResponse())
    )

    private fun vistaResponse() = VistaOrdenResponse(
        otId = 1,
        patente = "ABC123",
        nombreCliente = "Cliente",
        estado = "PENDIENTE",
        fechaIngreso = "2026-11-22T10:30:00",
        total = 11900,
        vehiculoId = 2
    )

    private fun lineaResponse() = LineaResponse(
        id = 4,
        otId = 1,
        tipo = "REPUESTO",
        descripcion = "Pastillas",
        costo = 10000,
        factor = BigDecimal("1.30"),
        cobrado = 13000
    )

    private fun evidenciaResponse() = EvidenciaResponse(
        id = 3,
        otId = 1,
        tipo = "VEHICULO",
        ruta = "/tmp/foto.jpg",
        fecha = "2026-11-22T10:40:00"
    )

    private fun historialResponse() = HistorialResponse(
        id = 2,
        otId = 1,
        estadoAnterior = "PENDIENTE",
        estadoNuevo = "EN_PROCESO",
        fechaCambio = "2026-11-22T11:00:00",
        observacion = "Inicio de trabajos"
    )
}
