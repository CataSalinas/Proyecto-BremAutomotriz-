package com.example.bremautomotriz.ui.viewmodel

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.SessionStore
import com.example.bremautomotriz.data.repository.AuthRepository
import com.example.bremautomotriz.data.repository.ClienteRepository
import com.example.bremautomotriz.data.repository.IndicadorRepository
import com.example.bremautomotriz.data.repository.OrdenRepository
import com.example.bremautomotriz.data.repository.VehiculoRepository
import com.example.bremautomotriz.domain.EstadoOt
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.model.Cliente
import com.example.bremautomotriz.model.DetalleOrden
import com.example.bremautomotriz.model.Indicador
import com.example.bremautomotriz.model.Indicadores
import com.example.bremautomotriz.model.Linea
import com.example.bremautomotriz.model.Orden
import com.example.bremautomotriz.model.UsuarioSesion
import com.example.bremautomotriz.model.Vehiculo
import com.example.bremautomotriz.model.VistaOrden
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class MainDispatcherTest {
    @Before
    fun prepararDispatcher() {
        Dispatchers.setMain(Dispatchers.Unconfined)
    }

    @After
    fun restaurarDispatcher() {
        Dispatchers.resetMain()
    }
}

class LoginViewModelTest : MainDispatcherTest() {

    private val repositorio = mockk<AuthRepository>(relaxed = true)

    @Test
    fun `valida credenciales antes de llamar al repositorio`() = runBlocking {
        val viewModel = LoginViewModel(repositorio)

        viewModel.iniciarSesion {}

        assertTrue(viewModel.uiState.value.emailError!!.contains("correo"))
        assertTrue(viewModel.uiState.value.passwordError!!.contains("contraseña"))
        coVerify(exactly = 0) { repositorio.login(any(), any()) }

        viewModel.onEmailChanged("correo-invalido")
        viewModel.onPasswordChanged("123")
        viewModel.iniciarSesion {}

        assertNotNull(viewModel.uiState.value.emailError)
        assertTrue(viewModel.uiState.value.passwordError!!.contains("8 caracteres"))
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login exitoso invoca callback`() = runBlocking {
        coEvery { repositorio.login("jefe@brem.cl", "jefe1234") } returns ApiResult.Ok(usuario())
        val viewModel = LoginViewModel(repositorio)
        var entrado = false

        viewModel.onEmailChanged("jefe@brem.cl")
        viewModel.onPasswordChanged("jefe1234")
        viewModel.iniciarSesion { entrado = true }

        assertTrue(entrado)
        assertFalse(viewModel.uiState.value.cargando)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login fallido muestra mensaje`() = runBlocking {
        coEvery { repositorio.login(any(), any()) } returns ApiResult.Fallo(401, "Credenciales inválidas")
        val viewModel = LoginViewModel(repositorio)

        viewModel.onEmailChanged("jefe@brem.cl")
        viewModel.onPasswordChanged("jefe1234")
        viewModel.iniciarSesion {}

        assertEquals("Credenciales inválidas", viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.cargando)
    }
}

class HomeViewModelTest : MainDispatcherTest() {

    private val indicadores = mockk<IndicadorRepository>(relaxed = true)
    private val sessionStore = mockk<SessionStore>(relaxed = true)
    private val auth = mockk<AuthRepository>(relaxed = true)

    @Test
    fun `no consulta indicadores al iniciar`() = runBlocking {
        val viewModel = HomeViewModel(indicadores, sessionStore, auth)

        assertNull(viewModel.uiState.value.indicadores)
        assertFalse(viewModel.uiState.value.cargando)
        coVerify(exactly = 0) { indicadores.consultar() }
    }

    @Test
    fun `muestra error de indicadores y permite reintentar`() = runBlocking {
        coEvery { indicadores.consultar() } returnsMany listOf(
            ApiResult.Fallo(null, "Sin conexión"),
            ApiResult.Ok(indicadores())
        )
        val viewModel = HomeViewModel(indicadores, sessionStore, auth)
        viewModel.cargarIndicadores()
        assertEquals("Sin conexión", viewModel.uiState.value.error)

        viewModel.cargarIndicadores()
        assertNotNull(viewModel.uiState.value.indicadores)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `expone sesion y cierra sesion`() = runBlocking {
        every { sessionStore.sesion } returns flowOf(usuario())
        coEvery { auth.cerrarSesion() } returns Unit
        val viewModel = HomeViewModel(indicadores, sessionStore, auth)
        var salio = false
        var ultimo: UsuarioSesion? = null
        val observador = launch { viewModel.usuario.collect { ultimo = it } }
        delay(50)

        assertEquals("Jefe", ultimo?.nombre)
        observador.cancel()

        viewModel.cerrarSesion { salio = true }
        assertTrue(salio)
        coVerify { auth.cerrarSesion() }
    }
}

class BuscarViewModelTest : MainDispatcherTest() {

    private val repositorio = mockk<OrdenRepository>(relaxed = true)

    @Test
    fun `busca al iniciar y al cambiar texto con debounce`() = runBlocking {
        coEvery { repositorio.buscar("") } returns ApiResult.Ok(listOf(vista()))
        coEvery { repositorio.buscar("ABC") } returns ApiResult.Ok(listOf(vista()))
        val viewModel = BuscarViewModel(repositorio)
        assertEquals(1, viewModel.uiState.value.resultados.size)

        viewModel.onTextoChanged(" ABC ")
        delay(100)
        coVerify(exactly = 1) { repositorio.buscar("") }
        delay(300)

        assertEquals(" ABC ", viewModel.uiState.value.texto)
        coVerify(exactly = 1) { repositorio.buscar("ABC") }
    }

    @Test
    fun `muestra error y reintenta`() = runBlocking {
        coEvery { repositorio.buscar("") } returnsMany listOf(
            ApiResult.Fallo(500, "Error del servidor"),
            ApiResult.Ok(listOf(vista()))
        )
        val viewModel = BuscarViewModel(repositorio)
        assertEquals("Error del servidor", viewModel.uiState.value.error)

        viewModel.reintentar()
        assertNull(viewModel.uiState.value.error)
        assertEquals(1, viewModel.uiState.value.resultados.size)
    }
}

class NuevaOtViewModelTest : MainDispatcherTest() {

    private val vehiculos = mockk<VehiculoRepository>(relaxed = true)
    private val clientes = mockk<ClienteRepository>(relaxed = true)
    private val ordenes = mockk<OrdenRepository>(relaxed = true)

    @Test
    fun `busca patente existente`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Ok(vehiculo())
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)

        viewModel.onPatenteChanged("abc123")
        esperarBusqueda()

        assertEquals(5L, viewModel.uiState.value.vehiculo?.id)
        assertFalse(viewModel.uiState.value.vehiculoNuevo)
        assertTrue(viewModel.uiState.value.vehiculoInfo!!.contains("Toyota"))
    }

    @Test
    fun `patente no registrada habilita datos de vehiculo nuevo`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Fallo(404, "No encontrado")
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)

        viewModel.onPatenteChanged("ABC123")
        esperarBusqueda()

        assertTrue(viewModel.uiState.value.vehiculoNuevo)
        assertTrue(viewModel.uiState.value.vehiculoInfo!!.contains("no registrada"))
    }

    @Test
    fun `error al buscar patente se muestra`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Fallo(500, "Error del servidor")
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)

        viewModel.onPatenteChanged("ABC123")
        esperarBusqueda()

        assertEquals("Error del servidor", viewModel.uiState.value.error)
    }

    @Test
    fun `valida datos antes de guardar`() = runBlocking {
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)

        viewModel.guardar {}

        assertNotNull(viewModel.uiState.value.patenteError)
        assertNotNull(viewModel.uiState.value.kilometrajeError)
        assertNotNull(viewModel.uiState.value.observacionError)
        coVerify(exactly = 0) { ordenes.crear(any(), any(), any()) }

        viewModel.onPatenteChanged("ABC123")
        viewModel.onKilometrajeChanged("100")
        viewModel.onObservacionChanged("Frenos")
        viewModel.guardar {}

        assertEquals("Busca la patente antes de continuar.", viewModel.uiState.value.patenteError)
    }

    @Test
    fun `guarda orden con vehiculo existente`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Ok(vehiculo())
        coEvery { ordenes.crear(5, 100, "Frenos") } returns ApiResult.Ok(7L)
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)
        var idCreado = 0L

        viewModel.onPatenteChanged("ABC123")
        esperarBusqueda()
        viewModel.onKilometrajeChanged("100")
        viewModel.onObservacionChanged("Frenos")
        viewModel.onRunChanged(" 12.345.678-9 ")
        viewModel.onMarcaChanged("Toyota")
        viewModel.onModeloChanged("Yaris")
        viewModel.onAnioChanged("2024a")
        viewModel.onNombreClienteChanged("Cliente")
        viewModel.guardar { idCreado = it }

        assertEquals(7L, idCreado)
        assertEquals("2024", viewModel.uiState.value.anio)
        assertEquals("12.345.678-9", viewModel.uiState.value.run)
        coVerify(exactly = 0) { clientes.crear(any(), any(), any(), any()) }
    }

    @Test
    fun `crea cliente y vehiculo cuando la patente es nueva`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Fallo(404, "No encontrado")
        coEvery { clientes.crear("Cliente", null, null, null) } returns ApiResult.Ok(cliente())
        coEvery { vehiculos.crear("ABC123", "Toyota", "Yaris", 2024, null, null, 9) } returns ApiResult.Ok(vehiculo())
        coEvery { ordenes.crear(5, 100, "Frenos") } returns ApiResult.Ok(7L)
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)
        var idCreado = 0L

        viewModel.onPatenteChanged("ABC123")
        esperarBusqueda()
        viewModel.onMarcaChanged("Toyota")
        viewModel.onModeloChanged("Yaris")
        viewModel.onAnioChanged("2024")
        viewModel.onNombreClienteChanged("Cliente")
        viewModel.onKilometrajeChanged("100")
        viewModel.onObservacionChanged("Frenos")
        viewModel.guardar { idCreado = it }

        assertEquals(7L, idCreado)
        coVerify { clientes.crear("Cliente", null, null, null) }
        coVerify { vehiculos.crear("ABC123", "Toyota", "Yaris", 2024, null, null, 9) }
    }

    @Test
    fun `muestra fallos al crear cliente vehiculo u orden`() = runBlocking {
        coEvery { vehiculos.porPatente("ABC123") } returns ApiResult.Fallo(404, "No encontrado")
        coEvery { clientes.crear(any(), any(), any(), any()) } returns ApiResult.Fallo(500, "Fallo cliente") andThen
            ApiResult.Ok(cliente())
        coEvery { vehiculos.crear(any(), any(), any(), any(), any(), any(), any()) } returns
            ApiResult.Fallo(500, "Fallo vehículo") andThen ApiResult.Ok(vehiculo())
        coEvery { ordenes.crear(any(), any(), any()) } returns ApiResult.Fallo(500, "Fallo orden")
        val viewModel = NuevaOtViewModel(vehiculos, clientes, ordenes)

        suspend fun llenar() {
            viewModel.onPatenteChanged("ABC123")
            esperarBusqueda()
            viewModel.onMarcaChanged("Toyota")
            viewModel.onModeloChanged("Yaris")
            viewModel.onAnioChanged("2024")
            viewModel.onNombreClienteChanged("Cliente")
            viewModel.onKilometrajeChanged("100")
            viewModel.onObservacionChanged("Frenos")
        }

        llenar()
        viewModel.guardar {}
        assertEquals("Fallo cliente", viewModel.uiState.value.error)

        llenar()
        viewModel.guardar {}
        assertEquals("Fallo vehículo", viewModel.uiState.value.error)

        llenar()
        viewModel.guardar {}
        assertEquals("Fallo orden", viewModel.uiState.value.error)
    }

    private suspend fun esperarBusqueda() {
        delay(350)
    }
}

class DetalleOtViewModelTest : MainDispatcherTest() {

    private val repositorio = mockk<OrdenRepository>(relaxed = true)

    @Test
    fun `carga detalle una sola vez por orden`() = runBlocking {
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)

        viewModel.cargar(1)
        assertEquals(7L, viewModel.uiState.value.detalle?.orden?.id)
        assertEquals("Frenos", viewModel.uiState.value.observacionIngreso)

        viewModel.cargar(1)
        coVerify(exactly = 1) { repositorio.detalle(1) }
    }

    @Test
    fun `muestra error y recarga`() = runBlocking {
        coEvery { repositorio.detalle(1) } returnsMany listOf(
            ApiResult.Fallo(500, "Error del servidor"),
            ApiResult.Ok(detalle())
        )
        val viewModel = DetalleOtViewModel(repositorio)

        viewModel.cargar(1)
        assertEquals("Error del servidor", viewModel.uiState.value.error)

        viewModel.recargar()
        assertNotNull(viewModel.uiState.value.detalle)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `edita campos del formulario de lineas`() = runBlocking {
        val viewModel = DetalleOtViewModel(repositorio)

        viewModel.onTipoChanged(TipoLinea.OM)
        viewModel.onDescripcionChanged("Mano de obra")
        viewModel.onMontoChanged("12a3")
        viewModel.onFactorChanged("1.3")
        viewModel.onObservacionIngresoChanged("Nuevo motivo")
        viewModel.onObservacionEntregaChanged("Entregado")
        viewModel.editarLinea(Linea(id = 4, tipo = "REPUESTO", descripcion = "Pastillas", costo = 10000, factor = 1.3))
        assertEquals(4L, viewModel.uiState.value.editandoLineaId)
        assertEquals("10000", viewModel.uiState.value.monto)
        assertEquals("1.3", viewModel.uiState.value.factor)

        viewModel.nuevaLinea()
        assertNull(viewModel.uiState.value.editandoLineaId)
        assertEquals("", viewModel.uiState.value.descripcion)
        assertEquals(TipoLinea.REPUESTO, viewModel.uiState.value.tipo)
    }

    @Test
    fun `valida y guarda lineas`() = runBlocking {
        coEvery { repositorio.crearLinea(1, any(), any(), any(), any()) } returns ApiResult.Ok(Linea(id = 4))
        coEvery { repositorio.actualizarLinea(1, 4, any(), any(), any(), any()) } returns ApiResult.Ok(Linea(id = 4))
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)
        viewModel.cargar(1)

        viewModel.guardarLinea()
        assertNotNull(viewModel.uiState.value.descripcionError)
        assertNotNull(viewModel.uiState.value.montoError)

        viewModel.onDescripcionChanged("Pastillas")
        viewModel.onMontoChanged("10000")
        viewModel.onFactorChanged("0.5")
        viewModel.guardarLinea()
        assertNotNull(viewModel.uiState.value.factorError)

        viewModel.onFactorChanged("1.3")
        viewModel.guardarLinea()
        assertEquals("Línea guardada.", viewModel.uiState.value.mensaje)
        coVerify { repositorio.crearLinea(1, TipoLinea.REPUESTO, "Pastillas", 10000, 1.3) }

        viewModel.editarLinea(Linea(id = 4, descripcion = "Pastillas", costo = 10000, factor = 1.3))
        viewModel.guardarLinea()
        coVerify { repositorio.actualizarLinea(1, 4, any(), "Pastillas", 10000, 1.3) }
    }

    @Test
    fun `elimina linea y muestra fallos`() = runBlocking {
        coEvery { repositorio.eliminarLinea(1, 4) } returnsMany listOf(
            ApiResult.Ok(Unit),
            ApiResult.Fallo(500, "No se pudo eliminar")
        )
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)
        viewModel.cargar(1)
        val linea = Linea(id = 4)

        viewModel.eliminarLinea(linea)
        assertEquals("Línea eliminada.", viewModel.uiState.value.mensaje)

        viewModel.eliminarLinea(linea)
        assertEquals("No se pudo eliminar", viewModel.uiState.value.error)

        coEvery { repositorio.crearLinea(any(), any(), any(), any(), any()) } returns
            ApiResult.Fallo(500, "No se pudo guardar")
        viewModel.onDescripcionChanged("Pastillas")
        viewModel.onMontoChanged("1000")
        viewModel.guardarLinea()
        assertEquals("No se pudo guardar", viewModel.uiState.value.error)

        viewModel.limpiarMensajes()
        assertNull(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.mensaje)
    }

    @Test
    fun `guarda observacion de ingreso`() = runBlocking {
        coEvery { repositorio.actualizarObservacion(1, any()) } returnsMany listOf(
            ApiResult.Ok(Orden(id = 1)),
            ApiResult.Fallo(500, "No se pudo actualizar")
        )
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)
        viewModel.cargar(1)

        viewModel.onObservacionIngresoChanged(" ")
        viewModel.guardarObservacion()
        assertNotNull(viewModel.uiState.value.observacionError)

        viewModel.onObservacionIngresoChanged("Frenos nuevos")
        viewModel.guardarObservacion()
        assertEquals("Observación actualizada.", viewModel.uiState.value.mensaje)

        viewModel.guardarObservacion()
        assertEquals("No se pudo actualizar", viewModel.uiState.value.error)
    }

    @Test
    fun `cambia estado y exige observacion de entrega`() = runBlocking {
        coEvery { repositorio.cambiarEstado(1, any(), any()) } returnsMany listOf(
            ApiResult.Ok(detalle()),
            ApiResult.Fallo(500, "No se pudo cambiar")
        )
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)
        viewModel.cargar(1)

        viewModel.cambiarEstado(EstadoOt.ENTREGADO)
        assertTrue(viewModel.uiState.value.observacionError!!.contains("obligatoria"))
        coVerify(exactly = 0) { repositorio.cambiarEstado(any(), any(), any()) }

        viewModel.onObservacionEntregaChanged("Entregado")
        viewModel.cambiarEstado(EstadoOt.ENTREGADO)
        assertEquals("Estado actualizado.", viewModel.uiState.value.mensaje)
        assertEquals("", viewModel.uiState.value.observacionEntrega)

        viewModel.onObservacionEntregaChanged("Anulada")
        viewModel.cambiarEstado(EstadoOt.ANULADA)
        assertEquals("No se pudo cambiar", viewModel.uiState.value.error)
    }

    @Test
    fun `registra evidencia y valida ruta`() = runBlocking {
        coEvery { repositorio.crearEvidencia(1, any(), any()) } returnsMany listOf(
            ApiResult.Ok(com.example.bremautomotriz.model.Evidencia(id = 3)),
            ApiResult.Fallo(500, "No se pudo registrar")
        )
        coEvery { repositorio.detalle(1) } returns ApiResult.Ok(detalle())
        val viewModel = DetalleOtViewModel(repositorio)
        viewModel.cargar(1)

        viewModel.agregarEvidencia(" ")
        assertTrue(viewModel.uiState.value.error!!.contains("ruta"))

        viewModel.agregarEvidencia("/tmp/foto.jpg")
        assertEquals("Evidencia registrada.", viewModel.uiState.value.mensaje)

        viewModel.agregarEvidencia("/tmp/foto2.jpg")
        assertEquals("No se pudo registrar", viewModel.uiState.value.error)
    }
}

class ClientesViewModelTest : MainDispatcherTest() {

    private val repositorio = mockk<ClienteRepository>(relaxed = true)

    @Test
    fun `carga lista al iniciar y muestra fallo`() = runBlocking {
        coEvery { repositorio.listar() } returnsMany listOf(
            ApiResult.Ok(listOf(cliente())),
            ApiResult.Fallo(500, "Error del servidor")
        )
        val viewModel = ClientesViewModel(repositorio)
        assertEquals(1, viewModel.uiState.value.clientes.size)

        viewModel.cargar()
        assertEquals("Error del servidor", viewModel.uiState.value.error)
    }

    @Test
    fun `edita y limpia formulario`() = runBlocking {
        val viewModel = ClientesViewModel(repositorio)

        viewModel.onNombreChanged("Nuevo")
        viewModel.onRunChanged(" 12.345.678-9 ")
        viewModel.onTelefonoChanged(" 912345678 ")
        viewModel.onCorreoChanged(" cliente@brem.cl ")
        assertEquals("Nuevo", viewModel.uiState.value.nombre)

        viewModel.editar(cliente())
        assertEquals(9L, viewModel.uiState.value.editandoId)
        assertEquals("Cliente", viewModel.uiState.value.nombre)

        viewModel.nuevo()
        assertNull(viewModel.uiState.value.editandoId)
        assertEquals("", viewModel.uiState.value.nombre)
    }

    @Test
    fun `valida y guarda cliente existente y nuevo`() = runBlocking {
        coEvery { repositorio.crear(any(), any(), any(), any()) } returns ApiResult.Ok(cliente().copy(id = 10))
        coEvery { repositorio.actualizar(9, any(), any(), any(), any()) } returns ApiResult.Ok(cliente())
        coEvery { repositorio.listar() } returns ApiResult.Ok(listOf(cliente()))
        val viewModel = ClientesViewModel(repositorio)

        viewModel.guardar()
        assertNotNull(viewModel.uiState.value.nombreError)

        viewModel.onNombreChanged("Cliente")
        viewModel.guardar()
        assertEquals("Cliente guardado.", viewModel.uiState.value.mensaje)
        coVerify { repositorio.crear("Cliente", "", "", "") }

        viewModel.editar(cliente())
        viewModel.onNombreChanged("Editado")
        viewModel.guardar()
        coVerify { repositorio.actualizar(9, "Editado", "12.345.678-9", "", "") }
    }

    @Test
    fun `muestra fallo al guardar cliente`() = runBlocking {
        coEvery { repositorio.crear(any(), any(), any(), any()) } returns ApiResult.Fallo(500, "No se pudo guardar")
        val viewModel = ClientesViewModel(repositorio)

        viewModel.onNombreChanged("Cliente")
        viewModel.guardar()
        assertEquals("No se pudo guardar", viewModel.uiState.value.error)

        viewModel.limpiarMensajes()
        assertNull(viewModel.uiState.value.error)
    }
}

class VehiculosViewModelTest : MainDispatcherTest() {

    private val vehiculos = mockk<VehiculoRepository>(relaxed = true)
    private val clientes = mockk<ClienteRepository>(relaxed = true)

    @Test
    fun `carga vehiculos y clientes al iniciar`() = runBlocking {
        coEvery { vehiculos.listar() } returns ApiResult.Ok(listOf(vehiculo()))
        coEvery { clientes.listar() } returns ApiResult.Ok(listOf(cliente()))
        val viewModel = VehiculosViewModel(vehiculos, clientes)

        assertEquals(1, viewModel.uiState.value.vehiculos.size)
        assertEquals(1, viewModel.uiState.value.clientes.size)
    }

    @Test
    fun `muestra fallos al cargar catalogos`() = runBlocking {
        coEvery { vehiculos.listar() } returnsMany listOf(
            ApiResult.Fallo(500, "Fallo vehículos"),
            ApiResult.Ok(listOf(vehiculo()))
        )
        coEvery { clientes.listar() } returns ApiResult.Ok(listOf(cliente()))
        val viewModel = VehiculosViewModel(vehiculos, clientes)
        assertEquals("Fallo vehículos", viewModel.uiState.value.error)

        coEvery { clientes.listar() } returns ApiResult.Fallo(500, "Fallo clientes")
        viewModel.cargar()
        assertEquals("Fallo clientes", viewModel.uiState.value.error)
    }

    @Test
    fun `edita y limpia formulario de vehiculo`() = runBlocking {
        val viewModel = VehiculosViewModel(vehiculos, clientes)

        viewModel.onPatenteChanged(" abc123 ")
        viewModel.onMarcaChanged("Toyota")
        viewModel.onModeloChanged("Yaris")
        viewModel.onAnioChanged("2024a")
        viewModel.onVinChanged(" VIN ")
        viewModel.onTipoChanged(" Auto ")
        viewModel.onClienteIdChanged(9)
        assertEquals("ABC123", viewModel.uiState.value.patente)
        assertEquals("2024", viewModel.uiState.value.anio)
        assertEquals("Auto", viewModel.uiState.value.tipo)

        viewModel.editar(vehiculo())
        assertEquals(5L, viewModel.uiState.value.editandoId)
        assertEquals("ABC123", viewModel.uiState.value.patente)

        viewModel.nuevo()
        assertNull(viewModel.uiState.value.editandoId)
        assertEquals("", viewModel.uiState.value.patente)
    }

    @Test
    fun `valida y guarda vehiculo nuevo y existente`() = runBlocking {
        coEvery { vehiculos.crear(any(), any(), any(), any(), any(), any(), any()) } returns ApiResult.Ok(vehiculo())
        coEvery { vehiculos.actualizar(8, any(), any(), any(), any(), any(), any(), any()) } returns ApiResult.Ok(vehiculo())
        coEvery { vehiculos.listar() } returns ApiResult.Ok(listOf(vehiculo()))
        coEvery { clientes.listar() } returns ApiResult.Ok(listOf(cliente()))
        val viewModel = VehiculosViewModel(vehiculos, clientes)

        viewModel.guardar()
        assertNotNull(viewModel.uiState.value.patenteError)
        assertNotNull(viewModel.uiState.value.marcaError)
        assertNotNull(viewModel.uiState.value.modeloError)
        assertNotNull(viewModel.uiState.value.anioError)
        assertNotNull(viewModel.uiState.value.clienteError)

        viewModel.onPatenteChanged("ABC123")
        viewModel.onMarcaChanged("Toyota")
        viewModel.onModeloChanged("Yaris")
        viewModel.onAnioChanged("2024")
        viewModel.onClienteIdChanged(9)
        viewModel.guardar()
        assertEquals("Vehículo guardado.", viewModel.uiState.value.mensaje)
        coVerify { vehiculos.crear("ABC123", "Toyota", "Yaris", 2024, "", "", 9) }

        viewModel.editar(vehiculo())
        viewModel.onModeloChanged("Corolla")
        viewModel.guardar()
        coVerify { vehiculos.actualizar(5, any(), any(), "Corolla", any(), any(), any(), any()) }
    }

    @Test
    fun `muestra fallo al guardar vehiculo`() = runBlocking {
        coEvery { vehiculos.crear(any(), any(), any(), any(), any(), any(), any()) } returns
            ApiResult.Fallo(500, "No se pudo guardar")
        val viewModel = VehiculosViewModel(vehiculos, clientes)

        viewModel.onPatenteChanged("ABC123")
        viewModel.onMarcaChanged("Toyota")
        viewModel.onModeloChanged("Yaris")
        viewModel.onAnioChanged("2024")
        viewModel.onClienteIdChanged(9)
        viewModel.guardar()
        assertEquals("No se pudo guardar", viewModel.uiState.value.error)

        viewModel.limpiarMensajes()
        assertNull(viewModel.uiState.value.error)
    }
}

private fun usuario() = UsuarioSesion(
    userId = 1,
    nombre = "Jefe",
    email = "jefe@brem.cl",
    rol = "JEFE",
    token = "token"
)

private fun indicadores() = Indicadores(
    uf = Indicador(codigo = "uf", valor = 41384.81, fecha = "2026-11-22T03:00:00.000Z"),
    dolar = Indicador(codigo = "dolar", valor = 1008.79, fecha = "2026-11-22T03:00:00.000Z")
)

private fun vista() = VistaOrden(
    otId = 7,
    patente = "ABC123",
    nombreCliente = "Cliente",
    estado = EstadoOt.PENDIENTE,
    fechaIngreso = "2026-11-22T10:30:00",
    total = 11900,
    vehiculoId = 5
)

private fun cliente() = Cliente(id = 9, nombre = "Cliente", run = "12.345.678-9")

private fun vehiculo() = Vehiculo(
    id = 5,
    patente = "ABC123",
    marca = "Toyota",
    modelo = "Yaris",
    anio = 2024,
    clienteId = 9,
    clienteNombre = "Cliente"
)

private fun detalle() = DetalleOrden(
    orden = Orden(
        id = 7,
        vehiculoId = 5,
        patente = "ABC123",
        nombreCliente = "Cliente",
        fechaIngreso = "2026-11-22T10:30:00",
        kilometraje = 100,
        observacionIngreso = "Frenos",
        estado = EstadoOt.PENDIENTE,
        neto = 10000,
        iva = 1900,
        total = 11900
    ),
    lineas = listOf(Linea(id = 4, otId = 7, tipo = TipoLinea.REPUESTO, descripcion = "Pastillas", costo = 10000, cobrado = 13000)),
    historial = emptyList()
)
