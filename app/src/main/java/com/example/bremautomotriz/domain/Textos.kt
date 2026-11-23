package com.example.bremautomotriz.domain

import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Textos {
    private val local = Locale.forLanguageTag("es-CL")
    private val formato = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm", local)

    fun estado(estado: String): String = when (estado) {
        EstadoOt.PENDIENTE -> "Pendiente"
        EstadoOt.EN_PROCESO -> "En proceso"
        EstadoOt.LISTO -> "Listo"
        EstadoOt.ENTREGADO -> "Entregado"
        EstadoOt.ANULADA -> "Anulada"
        else -> estado
    }

    fun tipoLinea(tipo: String): String = when (tipo) {
        TipoLinea.OM -> "Mano de obra"
        TipoLinea.OMEX -> "OMEX"
        TipoLinea.REPUESTO -> "Repuesto"
        TipoLinea.INSUMO -> "Insumo"
        else -> tipo
    }

    fun pesos(valor: Int): String = "$" + NumberFormat.getIntegerInstance(local).format(valor)

    fun pesos(valor: Double): String = "$" + NumberFormat.getNumberInstance(local).apply {
        maximumFractionDigits = 2
    }.format(valor)

    fun fecha(iso: String?): String = if (iso.isNullOrBlank()) {
        "-"
    } else {
        runCatching { LocalDateTime.parse(iso.substringBefore("Z").take(19)).format(formato) }
            .getOrDefault(iso)
    }
}
