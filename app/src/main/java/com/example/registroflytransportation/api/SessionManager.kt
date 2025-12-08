package com.example.registroflytransportation.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_prefs")

class SessionManager(private val context: Context) {

    companion object {
        val USER_TOKEN = stringPreferencesKey("user_token")
        val USER_ID = longPreferencesKey("user_id") // Nueva clave para el ID de usuario
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit {
            it[USER_TOKEN] = token
        }
    }

    val authToken: Flow<String?> = context.dataStore.data.map {
        it[USER_TOKEN]
    }

    suspend fun getAuthToken(): String? {
        return authToken.first()
    }

    suspend fun saveUserId(id: Long) { // Nueva función para guardar el ID
        context.dataStore.edit {
            it[USER_ID] = id
        }
    }

    val userId: Flow<Long?> = context.dataStore.data.map { // Nuevo Flow para obtener el ID
        it[USER_ID]
    }

    suspend fun getUserId(): Long? {
        return userId.first()
    }

    suspend fun clearSession() {
        context.dataStore.edit {
            it.clear()
        }
    }
}