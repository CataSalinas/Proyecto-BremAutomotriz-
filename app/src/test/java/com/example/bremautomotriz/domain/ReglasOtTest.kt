package com.example.bremautomotriz.domain

import com.example.bremautomotriz.data.entity.EstadoOt
import com.example.bremautomotriz.data.entity.Rol
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReglasOtTest {

    @Test
    fun soloAdministradorPuedeEliminar() {
        assertTrue(ReglasOt.puedeEliminar(Rol.ADMIN))
        assertFalse(ReglasOt.puedeEliminar(Rol.JEFE))
        assertFalse(ReglasOt.puedeEliminar(null))
    }

    @Test
    fun jefeNoPuedeMarcarComoListo() {
        assertTrue(ReglasOt.puedeAvanzar(EstadoOt.EN_PROCESO, Rol.ADMIN))
        assertFalse(ReglasOt.puedeAvanzar(EstadoOt.EN_PROCESO, Rol.JEFE))
    }
}
