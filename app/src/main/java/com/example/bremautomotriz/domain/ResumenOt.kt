package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.LineaOt
import com.example.bremautomotriz.data.entity.OrdenTrabajo

object ResumenOt {

    fun texto(
        orden: OrdenTrabajo,
        lineas: List<LineaOt>,
        neto: Int,
        iva: Int,
        total: Int
    ): String {
        val detalle = if (lineas.isEmpty()) {
            "Sin trabajos ni repuestos registrados."
        } else {
            lineas.joinToString("\n") {
                "- ${Textos.tipoLinea(it.tipo)}: ${it.descripcion} (${Textos.pesos(it.cobrado)})"
            }
        }

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
            appendLine("Neto: ${Textos.pesos(neto)}")
            appendLine("IVA (19%): ${Textos.pesos(iva)}")
            append("Total: ${Textos.pesos(total)}")
        }
    }
}