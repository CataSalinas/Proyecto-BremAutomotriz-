package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.EstadoOt

object Textos {
    fun estado(estado: String): String = when (estado) {
        EstadoOt.PENDIENTE -> "Pendiente"
        EstadoOt.EN_PROCESO -> "En proceso"
        EstadoOt.LISTO -> "Listo"
        EstadoOt.ENTREGADO -> "Entregado"
        EstadoOt.ANULADA -> "Anulada"
        else -> estado
    }
}