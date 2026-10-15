package com.example.bremautomotriz.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "evidencias_ot",
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
data class EvidenciaOt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val otId: Long,
    val tipo: String,
    val uri: String,
    val fecha: Long = System.currentTimeMillis()
)