package com.example.cloty_administrador.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.cloty_administrador.data.api.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cloty_admin_session")

class TokenStore(private val context: Context) {

    @Volatile
    private var cachedToken: String? = null

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN].also { cachedToken = it }
    }

    val rolFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROL]
    }

    fun peekToken(): String? = cachedToken

    suspend fun saveSession(token: String, rol: String) {
        cachedToken = token
        ApiClient.setBearerToken(token)
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_ROL] = rol
        }
    }

    suspend fun clear() {
        cachedToken = null
        ApiClient.setBearerToken(null)
        context.dataStore.edit {
            it.remove(KEY_TOKEN)
            it.remove(KEY_ROL)
        }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
        private val KEY_ROL = stringPreferencesKey("user_rol")

        const val ROL_SUPER_USUARIO = "SUPER_USUARIO"
        const val ROL_ADMINISTRADOR = "ADMINISTRADOR"
    }
}
