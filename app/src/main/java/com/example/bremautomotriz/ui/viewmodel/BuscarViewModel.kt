package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.model.BuscarModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class BuscarViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.obtener(application).ordenTrabajoDao()
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
}