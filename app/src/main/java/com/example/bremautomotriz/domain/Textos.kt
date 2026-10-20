package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.EstadoOt
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Textos {
    private val local = Locale.forLanguageTag("es-CL")

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

    fun pesos(valor: Int): String =
        "\$" + NumberFormat.getIntegerInstance(local).format(valor)

    fun fecha(milis: Long): String =
        SimpleDateFormat("dd-MM-yyyy HH:mm", local).format(Date(milis))
}