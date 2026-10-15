package com.example.bremautomotriz.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.bremautomotriz.data.entity.Usuario

@Dao
interface UsuarioDao {
    @Insert
    suspend fun insertar(usuario: Usuario): Long

    @Query("SELECT * FROM usuarios WHERE email = :email AND activo = 1 LIMIT 1")
    suspend fun buscarPorEmail(email: String): Usuario?

    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int
}