package com.example.bremautomotriz.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculadoraMontosTest {

    @Test
    fun ejemploDelDocumento() {
        val resultado = CalculadoraMontos.calcular(
            listOf(
                LineaCalculo(TipoLinea.OM, 30000),
                LineaCalculo(TipoLinea.OMEX, 14000),
                LineaCalculo(TipoLinea.REPUESTO, 28000),
                LineaCalculo(TipoLinea.INSUMO, 10000)
            )
        )
        assertEquals(94600, resultado.neto)
        assertEquals(17974, resultado.iva)
        assertEquals(112574, resultado.total)
        assertEquals(4200, resultado.comisionOmex)
        assertEquals(8400, resultado.comisionRepuestos)
    }

    @Test
    fun sinLineasDaCero() {
        val resultado = CalculadoraMontos.calcular(emptyList())
        assertEquals(0, resultado.neto)
        assertEquals(0, resultado.iva)
        assertEquals(0, resultado.total)
    }

    @Test
    fun insumoOmitidoNoSuma() {
        val resultado = CalculadoraMontos.calcular(
            listOf(LineaCalculo(TipoLinea.OM, 20000))
        )
        assertEquals(20000, resultado.neto)
        assertEquals(3800, resultado.iva)
        assertEquals(23800, resultado.total)
    }

    @Test
    fun factorPropioDeUnaLinea() {
        val linea = LineaCalculo(TipoLinea.REPUESTO, 14000, 1.5)
        assertEquals(21000, CalculadoraMontos.cobrado(linea))
        assertEquals(7000, CalculadoraMontos.comision(linea))
    }

    @Test
    fun redondeaAlPesoMasCercano() {
        val resultado = CalculadoraMontos.calcular(
            listOf(LineaCalculo(TipoLinea.REPUESTO, 1001))
        )
        assertEquals(1301, resultado.neto)
        assertEquals(247, resultado.iva)
        assertEquals(1548, resultado.total)
    }
}