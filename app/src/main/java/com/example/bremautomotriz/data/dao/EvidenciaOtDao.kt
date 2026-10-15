package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bremautomotriz.data.entity.EvidenciaOt
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaOtDao {
    @Insert
    suspend fun insertar(evidencia: EvidenciaOt): Long

    @Query("SELECT * FROM evidencias_ot WHERE otId = :otId ORDER BY fecha")
    fun observarPorOt(otId: Long): Flow<List<EvidenciaOt>>

    @Query("SELECT COUNT(*) FROM evidencias_ot WHERE otId = :otId")
    suspend fun contarPorOt(otId: Long): Int
}