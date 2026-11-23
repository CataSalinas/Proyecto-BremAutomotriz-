package com.example.bremautomotriz.data.remote

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.bremautomotriz.model.UsuarioSesion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("brem_session")

private object Claves {
    val TOKEN = stringPreferencesKey("token")
    val ID = longPreferencesKey("userId")
    val NOMBRE = stringPreferencesKey("nombre")
    val EMAIL = stringPreferencesKey("email")
    val ROL = stringPreferencesKey("rol")
}

class SessionStore(
    private val context: Context,
    private val tokenHolder: TokenHolder
) {
    val sesion: Flow<UsuarioSesion?> = context.sessionDataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { prefs ->
            val token = prefs[Claves.TOKEN]
            if (token.isNullOrBlank()) {
                null
            } else {
                UsuarioSesion(
                    userId = prefs[Claves.ID] ?: 0,
                    nombre = prefs[Claves.NOMBRE].orEmpty(),
                    email = prefs[Claves.EMAIL].orEmpty(),
                    rol = prefs[Claves.ROL].orEmpty(),
                    token = token
                )
            }
        }

    suspend fun guardar(usuario: UsuarioSesion) {
        context.sessionDataStore.edit { prefs ->
            prefs[Claves.TOKEN] = usuario.token
            prefs[Claves.ID] = usuario.userId
            prefs[Claves.NOMBRE] = usuario.nombre
            prefs[Claves.EMAIL] = usuario.email
            prefs[Claves.ROL] = usuario.rol
        }
        tokenHolder.token = usuario.token
    }

    suspend fun limpiar() {
        context.sessionDataStore.edit { it.clear() }
        tokenHolder.token = null
    }

    suspend fun obtener(): UsuarioSesion? = sesion.firstOrNull()
}
