package com.example.bremautomotriz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lineas_ot",
    foreignKeys = [
        ForeignKey(
            entity = OrdenTrabajo::class,
            parentColumns = ["id"],
            childColumns = ["otId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["otId"])]
)
data class LineaOt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val otId: Long,
    val tipo: String,
    val descripcion: String,
    val costo: Int,
    val factor: Double = 1.3,
    val cobrado: Int
)