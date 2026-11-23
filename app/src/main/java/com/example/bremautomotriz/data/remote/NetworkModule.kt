package com.example.bremautomotriz.data.remote

import com.example.bremautomotriz.BuildConfig
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun gson() = GsonBuilder().serializeNulls().create()

    fun bremApi(tokenHolder: TokenHolder, eventos: AuthEvents): BremApi {
        val cliente = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenHolder, eventos))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl(conectarBarra(BuildConfig.BASE_URL))
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create(gson()))
            .build()
            .create(BremApi::class.java)
    }

    fun mindicadorApi(): MindicadorApi {
        val cliente = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
        return Retrofit.Builder()
            .baseUrl("https://mindicador.cl/")
            .client(cliente)
            .addConverterFactory(GsonConverterFactory.create(gson()))
            .build()
            .create(MindicadorApi::class.java)
    }

    private fun conectarBarra(url: String): String = if (url.endsWith("/")) url else "$url/"
}

object JwtDecoder {
    fun userId(token: String): Long = runCatching {
        val carga = token.split(".").getOrNull(1) ?: return@runCatching 0L
        val json = String(Base64.getUrlDecoder().decode(carga), StandardCharsets.UTF_8)
        JsonParser.parseString(json).asJsonObject.get("userId")?.asLong ?: 0L
    }.getOrDefault(0L)
}
