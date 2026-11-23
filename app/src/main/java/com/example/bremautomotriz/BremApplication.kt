package com.example.bremautomotriz

import android.app.Application
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.bremautomotriz.data.remote.AuthEvents
import com.example.bremautomotriz.data.remote.BremApi
import com.example.bremautomotriz.data.remote.MindicadorApi
import com.example.bremautomotriz.data.remote.NetworkModule
import com.example.bremautomotriz.data.remote.SessionStore
import com.example.bremautomotriz.data.remote.TokenHolder
import com.example.bremautomotriz.data.repository.AuthRepositoryImpl
import com.example.bremautomotriz.data.repository.ClienteRepositoryImpl
import com.example.bremautomotriz.data.repository.IndicadorRepositoryImpl
import com.example.bremautomotriz.data.repository.OrdenRepositoryImpl
import com.example.bremautomotriz.data.repository.VehiculoRepositoryImpl
import com.example.bremautomotriz.ui.viewmodel.BuscarViewModel
import com.example.bremautomotriz.ui.viewmodel.ClientesViewModel
import com.example.bremautomotriz.ui.viewmodel.DetalleOtViewModel
import com.example.bremautomotriz.ui.viewmodel.HomeViewModel
import com.example.bremautomotriz.ui.viewmodel.LoginViewModel
import com.example.bremautomotriz.ui.viewmodel.NuevaOtViewModel
import com.example.bremautomotriz.ui.viewmodel.VehiculosViewModel

class BremApplication : Application() {

    lateinit var contenedor: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        contenedor = AppContainer(applicationContext)
    }
}

class AppContainer(context: Context) {

    val tokenHolder = TokenHolder()
    val eventos = AuthEvents()
    val sessionStore = SessionStore(context, tokenHolder)

    val api: BremApi = NetworkModule.bremApi(tokenHolder, eventos)
    val apiExterna: MindicadorApi = NetworkModule.mindicadorApi()

    val authRepositorio = AuthRepositoryImpl(api, sessionStore)
    val ordenRepositorio = OrdenRepositoryImpl(api)
    val clienteRepositorio = ClienteRepositoryImpl(api)
    val vehiculoRepositorio = VehiculoRepositoryImpl(api)
    val indicadorRepositorio = IndicadorRepositoryImpl(apiExterna)

    lateinit var factoria: ViewModelProvider.Factory
        private set

    init {
        factoria = FactoriaViewModels(this)
    }
}

class FactoriaViewModels(private val contenedor: AppContainer) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = when {
        modelClass.isAssignableFrom(LoginViewModel::class.java) ->
            LoginViewModel(contenedor.authRepositorio) as T
        modelClass.isAssignableFrom(HomeViewModel::class.java) ->
            HomeViewModel(
                contenedor.indicadorRepositorio,
                contenedor.sessionStore,
                contenedor.authRepositorio
            ) as T
        modelClass.isAssignableFrom(BuscarViewModel::class.java) ->
            BuscarViewModel(contenedor.ordenRepositorio) as T
        modelClass.isAssignableFrom(NuevaOtViewModel::class.java) ->
            NuevaOtViewModel(
                contenedor.vehiculoRepositorio,
                contenedor.clienteRepositorio,
                contenedor.ordenRepositorio
            ) as T
        modelClass.isAssignableFrom(DetalleOtViewModel::class.java) ->
            DetalleOtViewModel(contenedor.ordenRepositorio) as T
        modelClass.isAssignableFrom(ClientesViewModel::class.java) ->
            ClientesViewModel(contenedor.clienteRepositorio) as T
        modelClass.isAssignableFrom(VehiculosViewModel::class.java) ->
            VehiculosViewModel(contenedor.vehiculoRepositorio, contenedor.clienteRepositorio) as T
        else -> throw IllegalArgumentException("ViewModel no registrado: ${modelClass.name}")
    }
}
