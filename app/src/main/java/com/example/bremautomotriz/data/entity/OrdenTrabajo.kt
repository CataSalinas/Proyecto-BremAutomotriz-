package com.example.bremautomotriz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ordenes_trabajo",
    foreignKeys = [
        ForeignKey(
            entity = Vehiculo::class,
            parentColumns = ["id"],
            childColumns = ["vehiculoId"]
        )
    ],
    indices = [Index(value = ["vehiculoId"])]
)
data class OrdenTrabajo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehiculoId: Long,
    val patente: String,
    val nombreCliente: String,
    val fechaIngreso: Long = System.currentTimeMillis(),
    val fechaRegistro: Long = System.currentTimeMillis(),
    val kilometraje: Int,
    val observacionIngreso: String = "",
    val observacionEntrega: String = "",
    val estado: String = EstadoOt.PENDIENTE,
    val neto: Int = 0,
    val iva: Int = 0,
    val total: Int = 0
)