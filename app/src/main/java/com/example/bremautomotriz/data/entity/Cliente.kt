package com.example.bremautomotriz.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clientes")
data class Cliente(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val run: String,
    val telefono: String = "",
    val correo: String = ""
)