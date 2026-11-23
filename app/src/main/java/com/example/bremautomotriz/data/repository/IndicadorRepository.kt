package com.example.bremautomotriz.data.repository

import com.example.bremautomotriz.data.remote.ApiResult
import com.example.bremautomotriz.data.remote.MindicadorApi
import com.example.bremautomotriz.data.remote.ejecutarLlamada
import com.example.bremautomotriz.model.Indicadores
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

interface IndicadorRepository {
    suspend fun consultar(): ApiResult<Indicadores>
}

class IndicadorRepositoryImpl(private val api: MindicadorApi) : IndicadorRepository {

    override suspend fun consultar(): ApiResult<Indicadores> = ejecutarLlamada {
        coroutineScope {
            val uf = async { api.uf().aModelo() }
            val dolar = async { api.dolar().aModelo() }
            Indicadores(uf = uf.await(), dolar = dolar.await())
        }
    }
}
