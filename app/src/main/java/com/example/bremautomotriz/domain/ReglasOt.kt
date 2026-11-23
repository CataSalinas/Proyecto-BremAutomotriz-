package com.example.bremautomotriz.domain

object ReglasOt {

    private val finales = setOf(EstadoOt.ENTREGADO, EstadoOt.ANULADA)

    fun siguiente(estado: String): String? = when (estado) {
        EstadoOt.PENDIENTE -> EstadoOt.EN_PROCESO
        EstadoOt.EN_PROCESO -> EstadoOt.LISTO
        EstadoOt.LISTO -> EstadoOt.ENTREGADO
        else -> null
    }

    fun puedeAvanzar(estado: String, rol: String?): Boolean =
        rol in setOf(Rol.ADMIN, Rol.JEFE) && siguiente(estado) != null

    fun puedeAnular(estado: String, rol: String?): Boolean =
        rol in setOf(Rol.ADMIN, Rol.JEFE) && estado !in finales

    fun puedeEditar(estado: String): Boolean = estado !in finales

    fun esFinal(estado: String): Boolean = estado in finales
}
