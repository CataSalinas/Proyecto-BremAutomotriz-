package com.example.bremautomotriz.model

import com.example.bremautomotriz.data.entity.OrdenTrabajo

data class BuscarModel(
    val texto: String = "",
    val resultados: List<OrdenTrabajo> = emptyList()
)