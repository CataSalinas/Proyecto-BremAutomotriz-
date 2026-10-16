package com.example.bremautomotriz.domain

import kotlin.math.roundToInt

object TipoLinea {
    const val OM = "OM"
    const val OMEX = "OMEX"
    const val REPUESTO = "REPUESTO"
    const val INSUMO = "INSUMO"
}

data class LineaCalculo(
    val tipo: String,
    val monto: Int,
    val factor: Double = CalculadoraMontos.FACTOR_POR_DEFECTO
)

data class ResultadoMontos(
    val neto: Int,
    val iva: Int,
    val total: Int,
    val comisionOmex: Int,
    val comisionRepuestos: Int
)

object CalculadoraMontos {
    const val IVA_PORCENTAJE = 19
    const val FACTOR_POR_DEFECTO = 1.3
    const val INSUMO_POR_DEFECTO = 10000

    fun cobrado(linea: LineaCalculo): Int =
        when (linea.tipo) {
            TipoLinea.OMEX, TipoLinea.REPUESTO -> (linea.monto * linea.factor).roundToInt()
            else -> linea.monto
        }

    fun comision(linea: LineaCalculo): Int =
        when (linea.tipo) {
            TipoLinea.OMEX, TipoLinea.REPUESTO -> cobrado(linea) - linea.monto
            else -> 0
        }

    fun calcularIva(neto: Int): Int = (neto * IVA_PORCENTAJE + 50) / 100

    fun calcular(lineas: List<LineaCalculo>): ResultadoMontos {
        val neto = lineas.sumOf { cobrado(it) }
        val iva = calcularIva(neto)
        return ResultadoMontos(
            neto = neto,
            iva = iva,
            total = neto + iva,
            comisionOmex = lineas.filter { it.tipo == TipoLinea.OMEX }.sumOf { comision(it) },
            comisionRepuestos = lineas.filter { it.tipo == TipoLinea.REPUESTO }.sumOf { comision(it) }
        )
    }
}