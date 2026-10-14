package com.example.bremautomotriz.ui.navigation

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val BUSCAR = "buscar"
    const val NUEVA_OT = "nueva_ot"
    const val DETALLE_OT = "ot/{id}"

    fun detalleOt(id: Long) = "ot/$id"
}
