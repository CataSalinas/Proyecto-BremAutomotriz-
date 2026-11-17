package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.data.Fotos
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.data.entity.OrdenTrabajo
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.model.BuscarModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private val dao = db.ordenTrabajoDao()
    private val texto = MutableStateFlow("")

    var uiState by mutableStateOf(BuscarModel())
        private set

    init {
        viewModelScope.launch {
            texto
                .flatMapLatest { t ->
                    if (t.isBlank()) dao.listar() else dao.buscar(t.trim())
                }
                .collect { lista ->
                    uiState = uiState.copy(resultados = lista)
                }
        }
    }

    fun onTextoChanged(nuevo: String) {
        texto.value = nuevo
        uiState = uiState.copy(texto = nuevo)
    }

    fun onEliminar(orden: OrdenTrabajo, onEliminado: () -> Unit) {
        if (!ReglasOt.puedeEliminar(Sesion.usuario?.rol)) return
        viewModelScope.launch {
            db.evidenciaOtDao().listarPorOt(orden.id).forEach { Fotos.eliminar(it.uri) }
            dao.eliminar(orden.id)
            onEliminado()
        }
    }
}