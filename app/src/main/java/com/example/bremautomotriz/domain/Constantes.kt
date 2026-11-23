package com.example.bremautomotriz.domain

object Rol {
    const val ADMIN = "ADMIN"
    const val JEFE = "JEFE"
}

object EstadoOt {
    const val PENDIENTE = "PENDIENTE"
    const val EN_PROCESO = "EN_PROCESO"
    const val LISTO = "LISTO"
    const val ENTREGADO = "ENTREGADO"
    const val ANULADA = "ANULADA"
}

object TipoLinea {
    const val OM = "OM"
    const val OMEX = "OMEX"
    const val REPUESTO = "REPUESTO"
    const val INSUMO = "INSUMO"

    val TODOS = listOf(OM, OMEX, REPUESTO, INSUMO)
}

object TipoEvidencia {
    const val VEHICULO = "VEHICULO"
    const val NIVEL = "NIVEL"
    const val DOCUMENTO = "DOCUMENTO"
}
