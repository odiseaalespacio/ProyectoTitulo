package com.example.cloty_administrador.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cloty_admin_session")

class TokenStore(private val context: Context) {

    val tokenFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_TOKEN]
    }

    val rolFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[KEY_ROL]
    }

    suspend fun saveSession(token: String, rol: String) {
        context.dataStore.edit {
            it[KEY_TOKEN] = token
            it[KEY_ROL] = rol
        }
    }

    suspend fun clear() {
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
