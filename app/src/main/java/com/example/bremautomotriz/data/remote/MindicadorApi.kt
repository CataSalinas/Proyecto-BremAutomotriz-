package com.example.bremautomotriz.data.remote

import com.example.bremautomotriz.data.remote.dto.MindicadorResponse
import retrofit2.http.GET

interface MindicadorApi {
    @GET("api/uf")
    suspend fun uf(): MindicadorResponse

    @GET("api/dolar")
    suspend fun dolar(): MindicadorResponse
}
