package com.example.bremautomotriz.domain

import com.example.bremautomotriz.model.Linea
import com.example.bremautomotriz.model.Orden
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DomainTest {

    @Test
    fun `reglas de estado`() {
        assertEquals(EstadoOt.EN_PROCESO, ReglasOt.siguiente(EstadoOt.PENDIENTE))
        assertEquals(EstadoOt.LISTO, ReglasOt.siguiente(EstadoOt.EN_PROCESO))
        assertEquals(EstadoOt.ENTREGADO, ReglasOt.siguiente(EstadoOt.LISTO))
        assertNull(ReglasOt.siguiente(EstadoOt.ENTREGADO))
        assertNull(ReglasOt.siguiente(EstadoOt.ANULADA))
        assertNull(ReglasOt.siguiente("DESCONOCIDO"))

        assertTrue(ReglasOt.puedeAvanzar(EstadoOt.PENDIENTE, Rol.ADMIN))
        assertTrue(ReglasOt.puedeAvanzar(EstadoOt.LISTO, Rol.JEFE))
        assertFalse(ReglasOt.puedeAvanzar(EstadoOt.ENTREGADO, Rol.ADMIN))
        assertFalse(ReglasOt.puedeAvanzar(EstadoOt.PENDIENTE, "MECANICO"))
        assertFalse(ReglasOt.puedeAvanzar(EstadoOt.PENDIENTE, null))

        assertTrue(ReglasOt.puedeAnular(EstadoOt.EN_PROCESO, Rol.JEFE))
        assertFalse(ReglasOt.puedeAnular(EstadoOt.ANULADA, Rol.ADMIN))
        assertFalse(ReglasOt.puedeAnular(EstadoOt.PENDIENTE, null))

        assertTrue(ReglasOt.puedeEditar(EstadoOt.PENDIENTE))
        assertTrue(ReglasOt.puedeEditar(EstadoOt.EN_PROCESO))
        assertFalse(ReglasOt.puedeEditar(EstadoOt.ENTREGADO))
        assertFalse(ReglasOt.puedeEditar(EstadoOt.ANULADA))

        assertTrue(ReglasOt.esFinal(EstadoOt.ENTREGADO))
        assertTrue(ReglasOt.esFinal(EstadoOt.ANULADA))
        assertFalse(ReglasOt.esFinal(EstadoOt.LISTO))
    }

    @Test
    fun `validadores de credenciales y texto`() {
        assertTrue(Validadores.email("").orEmpty().contains("correo"))
        assertTrue(Validadores.email("correo").orEmpty().contains("válido"))
        assertNull(Validadores.email(" jefe@brem.cl "))

        assertTrue(Validadores.password("").orEmpty().contains("contraseña"))
        assertTrue(Validadores.password("corta").orEmpty().contains("8 caracteres"))
        assertNull(Validadores.password("jefe1234"))

        assertTrue(Validadores.texto(" ", "el nombre").orEmpty().contains("nombre"))
        assertTrue(Validadores.texto("a".repeat(101), "el nombre").orEmpty().contains("100"))
        assertNull(Validadores.texto("Cliente", "el nombre"))

        assertTrue(Validadores.patente("").orEmpty().contains("patente"))
        assertTrue(Validadores.patente("ABC-12").orEmpty().contains("6 caracteres"))
        assertNull(Validadores.patente("abc123"))
    }

    @Test
    fun `validadores de datos de orden y catalogos`() {
        assertTrue(Validadores.anio("abc").orEmpty().contains("válido"))
        assertTrue(Validadores.anio("1940").orEmpty().contains("1950"))
        assertNull(Validadores.anio("2026"))

        assertTrue(Validadores.kilometraje("abc").orEmpty().contains("número"))
        assertTrue(Validadores.kilometraje("2000000").orEmpty().contains("1999999"))
        assertNull(Validadores.kilometraje("0"))

        assertTrue(Validadores.observacion("").orEmpty().contains("motivo"))
        assertTrue(Validadores.observacion("a".repeat(501)).orEmpty().contains("500"))
        assertNull(Validadores.observacion("Frenos"))

        assertTrue(Validadores.descripcionLinea("").orEmpty().contains("descripción"))
        assertTrue(Validadores.descripcionLinea("a".repeat(101)).orEmpty().contains("100"))
        assertNull(Validadores.descripcionLinea("Cambio de aceite"))

        assertTrue(Validadores.monto("abc").orEmpty().contains("monto"))
        assertTrue(Validadores.monto("0").orEmpty().contains("mayor"))
        assertNull(Validadores.monto("15000"))

        assertNull(Validadores.factor(""))
        assertTrue(Validadores.factor("abc").orEmpty().contains("factor"))
        assertTrue(Validadores.factor("0.9").orEmpty().contains("1.00"))
        assertNull(Validadores.factor("1.30"))

        assertTrue(Validadores.ruta("").orEmpty().contains("ruta"))
        assertTrue(Validadores.ruta("a".repeat(256)).orEmpty().contains("255"))
        assertNull(Validadores.ruta("/tmp/foto.jpg"))
    }

    @Test
    fun `textos y resumen de orden`() {
        assertEquals("Pendiente", Textos.estado(EstadoOt.PENDIENTE))
        assertEquals("En proceso", Textos.estado(EstadoOt.EN_PROCESO))
        assertEquals("Listo", Textos.estado(EstadoOt.LISTO))
        assertEquals("Entregado", Textos.estado(EstadoOt.ENTREGADO))
        assertEquals("Anulada", Textos.estado(EstadoOt.ANULADA))
        assertEquals("OTRO", Textos.estado("OTRO"))

        assertEquals("Mano de obra", Textos.tipoLinea(TipoLinea.OM))
        assertEquals("OMEX", Textos.tipoLinea(TipoLinea.OMEX))
        assertEquals("Repuesto", Textos.tipoLinea(TipoLinea.REPUESTO))
        assertEquals("Insumo", Textos.tipoLinea(TipoLinea.INSUMO))
        assertEquals("OTRO", Textos.tipoLinea("OTRO"))

        assertTrue(Textos.pesos(1000).contains("1"))
        assertTrue(Textos.pesos(1000.5).contains("1"))
        assertEquals("-", Textos.fecha(null))
        assertEquals("-", Textos.fecha(" "))
        assertEquals("22-11-2026 03:00", Textos.fecha("2026-11-22T03:00:00.000Z"))
        assertEquals("22-11-2026 10:30", Textos.fecha("2026-11-22T10:30:00"))
        assertEquals("fecha-invalida", Textos.fecha("fecha-invalida"))

        val orden = Orden(
            id = 7,
            patente = "ABC123",
            nombreCliente = "Cliente",
            estado = EstadoOt.PENDIENTE,
            fechaIngreso = "2026-11-22T10:30:00",
            kilometraje = 120,
            observacionIngreso = "Frenos"
        )
        val sinLineas = ResumenOt.texto(orden, emptyList(), 1900, 11900)
        assertTrue(sinLineas.contains("Sin trabajos ni repuestos registrados."))
        assertTrue(sinLineas.contains("Orden de trabajo N° 7"))
        assertTrue(sinLineas.contains("IVA (19%)"))

        val conLineas = ResumenOt.texto(
            orden.copy(porcentajeIva = 19.0),
            listOf(Linea(tipo = TipoLinea.OM, descripcion = "Mano de obra", cobrado = 10000)),
            1900,
            11900
        )
        assertTrue(conLineas.contains("IVA (19%)"))
        assertTrue(conLineas.contains("Mano de obra: Mano de obra"))
        assertTrue(conLineas.contains("Total:"))
    }
}
