package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.LineaOt
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ResumenOtTest {

    private val orden = OrdenTrabajo(
        id = 7,
        vehiculoId = 1,
        patente = "ABCD12",
        nombreCliente = "Cliente Ejemplo Uno",
        kilometraje = 85000,
        observacionIngreso = "Cambio de aceite"
    )

    private val lineas = listOf(
        LineaOt(otId = 7, tipo = TipoLinea.OM, descripcion = "Mano de obra", costo = 50000, cobrado = 50000),
        LineaOt(otId = 7, tipo = TipoLinea.REPUESTO, descripcion = "Filtro", costo = 20000, cobrado = 26000)
    )

    @Test
    fun incluyeDatosYTotales() {
        val texto = ResumenOt.texto(orden, lineas, 86000, 16340, 102340)
        assertTrue(texto.contains("Orden de trabajo N° 7"))
        assertTrue(texto.contains("Patente: ABCD12"))
        assertTrue(texto.contains("Filtro (\$26.000)"))
        assertTrue(texto.contains("Total: \$102.340"))
    }

    @Test
    fun noMuestraElCostoInterno() {
        val texto = ResumenOt.texto(orden, lineas, 86000, 16340, 102340)
        assertFalse(texto.contains("20.000"))
    }

    @Test
    fun sinLineasAvisaQueNoHayDetalle() {
        val texto = ResumenOt.texto(orden, emptyList(), 0, 0, 0)
        assertTrue(texto.contains("Sin trabajos ni repuestos registrados."))
    }
}