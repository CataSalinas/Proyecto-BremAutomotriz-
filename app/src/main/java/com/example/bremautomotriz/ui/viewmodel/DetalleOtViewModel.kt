package com.example.bremautomotriz.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.repository.OrdenRepository
import com.example.bremautomotriz.domain.EstadoOt
import com.example.bremautomotriz.domain.TipoLinea
import com.example.bremautomotriz.domain.TipoEvidencia
import com.example.bremautomotriz.domain.Validadores
import com.example.bremautomotriz.model.DetalleOrden
import com.example.bremautomotriz.model.Linea
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetalleOtUiState(
    val detalle: DetalleOrden? = null,
    val cargando: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val tipo: String = TipoLinea.REPUESTO,
    val descripcion: String = "",
    val monto: String = "",
    val factor: String = "",
    val descripcionError: String? = null,
    val montoError: String? = null,
    val factorError: String? = null,
    val editandoLineaId: Long? = null,
    val observacionIngreso: String = "",
    val observacionEntrega: String = "",
    val observacionError: String? = null
)

class DetalleOtViewModel(private val repositorio: OrdenRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleOtUiState())
    val uiState: StateFlow<DetalleOtUiState> = _uiState.asStateFlow()

    private var idOt = 0L

    fun cargar(id: Long) {
        if (id == idOt && _uiState.value.detalle != null) return
        idOt = id
        recargar()
    }

    fun recargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            when (val resultado = repositorio.detalle(idOt)) {
                is ApiResult.Ok -> _uiState.update {
                    it.copy(
                        cargando = false,
                        detalle = resultado.valor,
                        observacionIngreso = resultado.valor.orden.observacionIngreso
                    )
                }
                is ApiResult.Fallo -> _uiState.update {
                    it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun onTipoChanged(valor: String) = _uiState.update { it.copy(tipo = valor) }
    fun onDescripcionChanged(valor: String) = _uiState.update {
        it.copy(descripcion = valor, descripcionError = null)
    }
    fun onMontoChanged(valor: String) = _uiState.update {
        it.copy(monto = valor.filter { c -> c.isDigit() }.take(9), montoError = null)
    }
    fun onFactorChanged(valor: String) = _uiState.update {
        it.copy(factor = valor, factorError = null)
    }
    fun onObservacionIngresoChanged(valor: String) = _uiState.update {
        it.copy(observacionIngreso = valor, observacionError = null)
    }
    fun onObservacionEntregaChanged(valor: String) = _uiState.update {
        it.copy(observacionEntrega = valor, observacionError = null)
    }

    fun editarLinea(linea: Linea) = _uiState.update {
        it.copy(
            tipo = linea.tipo,
            descripcion = linea.descripcion,
            monto = linea.costo.toString(),
            factor = if (linea.factor == 1.0) "" else linea.factor.toString(),
            editandoLineaId = linea.id,
            descripcionError = null,
            montoError = null,
            factorError = null
        )
    }

    fun nuevaLinea() = _uiState.update {
        it.copy(
            tipo = TipoLinea.REPUESTO,
            descripcion = "",
            monto = "",
            factor = "",
            editandoLineaId = null
        )
    }

    fun guardarLinea() {
        val estado = _uiState.value
        val descripcionError = Validadores.descripcionLinea(estado.descripcion)
        val montoError = Validadores.monto(estado.monto)
        val factorError = Validadores.factor(estado.factor)
        if (descripcionError != null || montoError != null || factorError != null) {
            _uiState.update {
                it.copy(
                    descripcionError = descripcionError,
                    montoError = montoError,
                    factorError = factorError
                )
            }
            return
        }
        val factor = estado.factor.toDoubleOrNull()
        viewModelScope.launch {
            val resultado = if (estado.editandoLineaId == null) {
                repositorio.crearLinea(
                    idOt, estado.tipo, estado.descripcion, estado.monto.toInt(), factor
                )
            } else {
                repositorio.actualizarLinea(
                    idOt, estado.editandoLineaId!!, estado.tipo, estado.descripcion,
                    estado.monto.toInt(), factor
                )
            }
            when (resultado) {
                is ApiResult.Ok -> {
                    _uiState.update {
                        it.copy(
                            descripcion = "", monto = "", factor = "", editandoLineaId = null,
                            descripcionError = null, montoError = null, factorError = null,
                            mensaje = "Línea guardada."
                        )
                    }
                    recargar()
                }
                is ApiResult.Fallo -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    fun eliminarLinea(linea: Linea) {
        viewModelScope.launch {
            when (val resultado = repositorio.eliminarLinea(idOt, linea.id)) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(mensaje = "Línea eliminada.") }
                    recargar()
                }
                is ApiResult.Fallo -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    fun guardarObservacion() {
        val observacion = _uiState.value.observacionIngreso
        val error = Validadores.observacion(observacion)
        if (error != null) {
            _uiState.update { it.copy(observacionError = error) }
            return
        }
        viewModelScope.launch {
            when (val resultado = repositorio.actualizarObservacion(idOt, observacion)) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(mensaje = "Observación actualizada.") }
                    recargar()
                }
                is ApiResult.Fallo -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    fun cambiarEstado(estadoNuevo: String) {
        val observacion = _uiState.value.observacionEntrega
        if (estadoNuevo == EstadoOt.ENTREGADO && observacion.isBlank()) {
            _uiState.update {
                it.copy(observacionError = "La observación de entrega es obligatoria.")
            }
            return
        }
        viewModelScope.launch {
            when (val resultado = repositorio.cambiarEstado(idOt, estadoNuevo, observacion)) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(mensaje = "Estado actualizado.", observacionEntrega = "") }
                    recargar()
                }
                is ApiResult.Fallo -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    fun agregarEvidencia(ruta: String) {
        val error = Validadores.ruta(ruta)
        if (error != null) {
            _uiState.update { it.copy(error = error) }
            return
        }
        viewModelScope.launch {
            when (val resultado = repositorio.crearEvidencia(idOt, ruta, TipoEvidencia.VEHICULO)) {
                is ApiResult.Ok -> {
                    _uiState.update { it.copy(mensaje = "Evidencia registrada.") }
                    recargar()
                }
                is ApiResult.Fallo -> _uiState.update { it.copy(error = resultado.mensaje) }
            }
        }
    }

    fun limpiarMensajes() = _uiState.update { it.copy(error = null, mensaje = null) }
}
