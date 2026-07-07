package com.example.cloty_colegio.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cloty_colegio_session")

class TokenStore(private val context: Context) {

    val tokenFlow: Flow<String?> = context.dataStore.data.map { it[KEY_TOKEN] }

    suspend fun saveToken(token: String) {
        context.dataStore.edit { prefs -> prefs[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        context.dataStore.edit { it.remove(KEY_TOKEN) }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
    }
}
