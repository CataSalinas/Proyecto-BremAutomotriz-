package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenTrabajoDao {
    @Insert
    suspend fun insertar(orden: OrdenTrabajo): Long

    @Update
    suspend fun actualizar(orden: OrdenTrabajo)

    @Query("SELECT * FROM ordenes_trabajo WHERE id = :id")
    suspend fun buscarPorId(id: Long): OrdenTrabajo?

    @Query("SELECT * FROM ordenes_trabajo WHERE id = :id")
    fun observarPorId(id: Long): Flow<OrdenTrabajo?>

    @Query("SELECT * FROM ordenes_trabajo ORDER BY id DESC")
    fun listar(): Flow<List<OrdenTrabajo>>

    @Query(
        "SELECT * FROM ordenes_trabajo " +
            "WHERE patente LIKE '%' || :texto || '%' " +
            "OR nombreCliente LIKE '%' || :texto || '%' " +
            "OR CAST(id AS TEXT) LIKE '%' || :texto || '%' " +
            "ORDER BY id DESC"
    )
    fun buscar(texto: String): Flow<List<OrdenTrabajo>>

    @Query("SELECT * FROM ordenes_trabajo WHERE vehiculoId = :vehiculoId AND id != :excluirId ORDER BY id DESC")
    fun observarHistorial(vehiculoId: Long, excluirId: Long): Flow<List<OrdenTrabajo>>

}