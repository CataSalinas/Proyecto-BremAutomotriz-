package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.bremautomotriz.data.entity.LineaOt
import kotlinx.coroutines.flow.Flow

@Dao
interface LineaOtDao {
    @Insert
    suspend fun insertar(linea: LineaOt): Long

    @Delete
    suspend fun eliminar(linea: LineaOt)

    @Query("SELECT * FROM lineas_ot WHERE otId = :otId ORDER BY id")
    fun observarPorOt(otId: Long): Flow<List<LineaOt>>

    @Query("SELECT * FROM lineas_ot WHERE otId = :otId ORDER BY id")
    suspend fun listarPorOt(otId: Long): List<LineaOt>
}