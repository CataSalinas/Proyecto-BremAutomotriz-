package com.example.bremautomotriz.data

import com.example.bremautomotriz.data.entity.Cliente
import com.example.bremautomotriz.data.entity.Rol
import com.example.bremautomotriz.data.entity.Usuario
import com.example.bremautomotriz.data.entity.Vehiculo

object DatosEjemplo {

    suspend fun cargarSiVacio(db: AppDatabase) {
        if (db.usuarioDao().contar() > 0) return

        db.usuarioDao().insertar(
            Usuario(nombre = "Administrador Uno", email = "admin1@brem.cl", password = "admin123", rol = Rol.ADMIN)
        )
        db.usuarioDao().insertar(
            Usuario(nombre = "Administrador Dos", email = "admin2@brem.cl", password = "admin123", rol = Rol.ADMIN)
        )
        db.usuarioDao().insertar(
            Usuario(nombre = "Jefe de Taller", email = "jefe@brem.cl", password = "jefe123", rol = Rol.JEFE)
        )
        db.usuarioDao().insertar(
            Usuario(nombre = "Developer", email = "dev@brem.cl", password = "dev1234", rol = Rol.DEVELOPER)
        )

        val cliente1 = db.clienteDao().insertar(
            Cliente(nombre = "Cliente Ejemplo Uno", run = "11.111.111-1", telefono = "+56 9 1111 1111", correo = "cliente1@ejemplo.cl")
        )
        val cliente2 = db.clienteDao().insertar(
            Cliente(nombre = "Cliente Ejemplo Dos", run = "22.222.222-2", telefono = "+56 9 2222 2222", correo = "cliente2@ejemplo.cl")
        )

        db.vehiculoDao().insertar(
            Vehiculo(patente = "ABCD12", marca = "Toyota", modelo = "Yaris", anio = 2018, tipo = "Auto", clienteId = cliente1)
        )
        db.vehiculoDao().insertar(
            Vehiculo(patente = "WXYZ34", marca = "Chevrolet", modelo = "Sail", anio = 2020, tipo = "Auto", clienteId = cliente1)
        )
        db.vehiculoDao().insertar(
            Vehiculo(patente = "BCDF56", marca = "Nissan", modelo = "NP300", anio = 2016, tipo = "Camioneta", clienteId = cliente2)
        )
    }
}