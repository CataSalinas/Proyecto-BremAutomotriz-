package com.example.bremautomotriz.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.AppDatabase
import com.example.bremautomotriz.data.Sesion
import com.example.bremautomotriz.data.entity.LineaOt
import com.example.bremautomotriz.domain.CalculadoraMontos
import com.example.bremautomotriz.domain.LineaCalculo
import com.example.bremautomotriz.domain.ReglasOt
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.DetalleOtModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class DetalleOtViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.obtener(application)
    private var otId = 0L
    private var observacion: Job? = null

    var uiState by mutableStateOf(DetalleOtModel())
        private set

    fun cargar(id: Long) {
        if (id == otId) return
        otId = id
        observacion?.cancel()
        observacion = viewModelScope.launch {
            launch {
                db.ordenTrabajoDao().observarPorId(id).collect { orden ->
                    uiState = uiState.copy(orden = orden)
                }
            }
            launch {
                db.ordenTrabajoDao().observarPorId(id)
                    .filterNotNull()
                    .flatMapLatest { db.ordenTrabajoDao().observarHistorial(it.vehiculoId, id) }
                    .collect { historial -> uiState = uiState.copy(historial = historial) }
            }
            launch {
                db.lineaOtDao().observarPorOt(id).collect { lista ->
                    val resultado = calcular(lista)
                    uiState = uiState.copy(
                        lineas = lista,
                        neto = resultado.neto,
                        iva = resultado.iva,
                        total = resultado.total
                    )
                }
            }
        }
    }

    fun onTipoChanged(tipo: String) {
        uiState = uiState.copy(tipo = tipo, montoError = null)
        if (tipo == TipoLinea.INSUMO) {
            uiState = uiState.copy(
                descripcion = uiState.descripcion.ifBlank { "Insumos de taller" },
                monto = CalculadoraMontos.INSUMO_POR_DEFECTO.toString(),
                descripcionError = null
            )
        }
    }

    fun onDescripcionChanged(valor: String) {
        uiState = uiState.copy(descripcion = valor, descripcionError = null)
    }

    fun onMontoChanged(valor: String) {
        uiState = uiState.copy(monto = valor.filter { it.isDigit() }.take(8), montoError = null)
    }

    fun onAgregarLinea() {
        val descripcionError = Validadores.descripcionLinea(uiState.descripcion)
        val montoError = Validadores.monto(uiState.monto)
        if (descripcionError != null || montoError != null) {
            uiState = uiState.copy(descripcionError = descripcionError, montoError = montoError)
            return
        }
        val tipo = uiState.tipo
        val monto = uiState.monto.toInt()
        val descripcion = uiState.descripcion.trim()
        viewModelScope.launch {
            db.lineaOtDao().insertar(
                LineaOt(
                    otId = otId,
                    tipo = tipo,
                    descripcion = descripcion,
                    costo = monto,
                    cobrado = CalculadoraMontos.cobrado(LineaCalculo(tipo, monto))
                )
            )
            uiState = uiState.copy(
                descripcion = "",
                monto = "",
                descripcionError = null,
                montoError = null
            )
            guardarTotales()
        }
    }

    fun onEliminarLinea(linea: LineaOt) {
        viewModelScope.launch {
            db.lineaOtDao().eliminar(linea)
            guardarTotales()
        }
    }

    fun onAvanzarEstado(onListo: (String) -> Unit) {
        viewModelScope.launch {
            val orden = db.ordenTrabajoDao().buscarPorId(otId) ?: return@launch
            val siguiente = ReglasOt.siguiente(orden.estado) ?: return@launch
            if (!ReglasOt.puedeAvanzar(orden.estado, Sesion.usuario?.rol)) return@launch
            db.ordenTrabajoDao().actualizar(orden.copy(estado = siguiente))
            onListo(siguiente)
        }
    }

    private fun calcular(lista: List<LineaOt>) =
        CalculadoraMontos.calcular(lista.map { LineaCalculo(it.tipo, it.costo, it.factor) })

    private suspend fun guardarTotales() {
        val orden = db.ordenTrabajoDao().buscarPorId(otId) ?: return
        val resultado = calcular(db.lineaOtDao().listarPorOt(otId))
        db.ordenTrabajoDao().actualizar(
            orden.copy(neto = resultado.neto, iva = resultado.iva, total = resultado.total)
        )
    }
}