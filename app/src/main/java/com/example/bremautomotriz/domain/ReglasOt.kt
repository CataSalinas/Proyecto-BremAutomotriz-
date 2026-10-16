package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.EstadoOt
import com.example.bremautomotriz.data.entity.Rol

object ReglasOt {

    fun siguiente(estado: String): String? = when (estado) {
        EstadoOt.PENDIENTE -> EstadoOt.EN_PROCESO
        EstadoOt.EN_PROCESO -> EstadoOt.LISTO
        EstadoOt.LISTO -> EstadoOt.ENTREGADO
        else -> null
    }

    fun puedeAvanzar(estado: String, rol: String?): Boolean {
        if (rol == null) return false
        return when (estado) {
            EstadoOt.PENDIENTE, EstadoOt.LISTO -> true
            EstadoOt.EN_PROCESO -> rol == Rol.ADMIN || rol == Rol.DEVELOPER
            else -> false
        }
    }

    fun puedeEditarLineas(estado: String): Boolean =
        estado == EstadoOt.PENDIENTE || estado == EstadoOt.EN_PROCESO
}