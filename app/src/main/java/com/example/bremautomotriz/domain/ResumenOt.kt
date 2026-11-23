package com.example.bremautomotriz.domain

import com.example.bremautomotriz.model.Linea
import com.example.bremautomotriz.model.Orden

object ResumenOt {

    fun texto(orden: Orden, lineas: List<Linea>, iva: Int, total: Int): String {
        val detalle = if (lineas.isEmpty()) {
            "Sin trabajos ni repuestos registrados."
        } else {
            lineas.joinToString("\n") {
                "- ${Textos.tipoLinea(it.tipo)}: ${it.descripcion} (${Textos.pesos(it.cobrado)})"
            }
        }
        val porcentaje = orden.porcentajeIva?.let { String.format("%,.0f", it) } ?: "19"

        return buildString {
            appendLine("BREM Automotriz")
            appendLine("Orden de trabajo N° ${orden.id}")
            appendLine("Estado: ${Textos.estado(orden.estado)}")
            appendLine("Patente: ${orden.patente}")
            appendLine("Cliente: ${orden.nombreCliente}")
            appendLine("Ingreso: ${Textos.fecha(orden.fechaIngreso)}")
            appendLine("Kilometraje: ${orden.kilometraje} km")
            appendLine("Motivo: ${orden.observacionIngreso}")
            appendLine()
            appendLine("Detalle:")
            appendLine(detalle)
            appendLine()
            appendLine("IVA ($porcentaje%): ${Textos.pesos(iva)}")
            append("Total: ${Textos.pesos(total)}")
        }
    }
}
