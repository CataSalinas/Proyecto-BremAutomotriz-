package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bremautomotriz.data.entity.Cliente
import kotlinx.coroutines.flow.Flow

@Dao
interface ClienteDao {
    @Insert
    suspend fun insertar(cliente: Cliente): Long

    @Query("SELECT * FROM clientes WHERE id = :id")
    suspend fun buscarPorId(id: Long): Cliente?

    @Query("SELECT * FROM clientes ORDER BY nombre")
    fun listar(): Flow<List<Cliente>>
}