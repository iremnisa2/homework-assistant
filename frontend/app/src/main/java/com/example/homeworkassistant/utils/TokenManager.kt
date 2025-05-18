package com.example.homeworkassistant.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.homeworkassistant.data.api.ApiClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore instance
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "homework_assistant_prefs")

class TokenManager(private val context: Context) {
    
    // Save token to DataStore
    suspend fun saveToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[ApiClient.AUTH_TOKEN_KEY] = token
        }
    }
    
    // Get token from DataStore as a Flow
    fun getToken(): Flow<String?> {
        return context.dataStore.data.map { preferences ->
            preferences[ApiClient.AUTH_TOKEN_KEY]
        }
    }
    
    // Clear token from DataStore
    suspend fun clearToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(ApiClient.AUTH_TOKEN_KEY)
        }
    }
    
    // Check if user is logged in
    fun isLoggedIn(): Flow<Boolean> {
        return getToken().map { token ->
            !token.isNullOrEmpty()
        }
    }
} 