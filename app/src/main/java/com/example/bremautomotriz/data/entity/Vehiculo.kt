package com.example.bremautomotriz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "vehiculos",
    foreignKeys = [
        ForeignKey(
            entity = Cliente::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"]
        )
    ],
    indices = [
        Index(value = ["patente"], unique = true),
        Index(value = ["clienteId"])
    ]
)
data class Vehiculo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patente: String,
    val marca: String,
    val modelo: String,
    val anio: Int,
    val vin: String = "",
    val tipo: String = "",
    val clienteId: Long
)
