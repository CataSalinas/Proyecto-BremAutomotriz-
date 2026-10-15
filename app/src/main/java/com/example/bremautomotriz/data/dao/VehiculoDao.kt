package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bremautomotriz.data.entity.Vehiculo
import kotlinx.coroutines.flow.Flow

@Dao
interface VehiculoDao {
    @Insert
    suspend fun insertar(vehiculo: Vehiculo): Long

    @Query("SELECT * FROM vehiculos WHERE patente = :patente LIMIT 1")
    suspend fun buscarPorPatente(patente: String): Vehiculo?

    @Query("SELECT * FROM vehiculos WHERE patente LIKE '%' || :texto || '%' ORDER BY patente")
    fun buscarPorTexto(texto: String): Flow<List<Vehiculo>>
}