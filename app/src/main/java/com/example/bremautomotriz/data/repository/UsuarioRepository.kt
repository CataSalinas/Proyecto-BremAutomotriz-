package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.dao.UsuarioDao
import com.example.bremautomotriz.data.entity.Usuario

class UsuarioRepository(private val usuarioDao: UsuarioDao) {

    suspend fun iniciarSesion(email: String, password: String): Usuario? {
        val usuario = usuarioDao.buscarPorEmail(email.trim().lowercase()) ?: return null
        return if (usuario.password == password) usuario else null
    }
}