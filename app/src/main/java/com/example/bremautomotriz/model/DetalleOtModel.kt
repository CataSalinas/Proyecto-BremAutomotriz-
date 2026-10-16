package com.example.bremautomotriz.model

import com.example.bremautomotriz.data.entity.LineaOt
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.domain.TipoLinea

data class DetalleOtModel(
    val orden: OrdenTrabajo? = null,
    val lineas: List<LineaOt> = emptyList(),
    val historial: List<OrdenTrabajo> = emptyList(),
    val neto: Int = 0,
    val iva: Int = 0,
    val total: Int = 0,
    val tipo: String = TipoLinea.REPUESTO,
    val descripcion: String = "",
    val monto: String = "",
    val descripcionError: String? = null,
    val montoError: String? = null
)