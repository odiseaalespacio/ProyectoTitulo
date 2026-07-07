package com.example.cloty_apoderado.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("cloty_apoderado_session")

class TokenStore(appContext: Context) {

    private val store = appContext.applicationContext.dataStore

    val tokenFlow: Flow<String?> = store.data.map { it[KEY_TOKEN] }

    suspend fun saveToken(token: String) {
        store.edit { it[KEY_TOKEN] = token }
    }

    suspend fun clear() {
        store.edit { it.remove(KEY_TOKEN) }
    }

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("jwt_token")
    }
}
