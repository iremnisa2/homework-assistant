package com.example.homeworkassistant.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

// Extension property for DataStore instance (using the same datastore as TokenManager)
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "homework_assistant_prefs")

class SessionManager(private val context: Context) {
    
    companion object {
        private val FIRST_TIME_LOGIN = booleanPreferencesKey("first_time_login")
    }
    
    // Set the first time login flag
    fun setFirstTimeLogin(isFirstTime: Boolean) = runBlocking {
        context.dataStore.edit { preferences ->
            preferences[FIRST_TIME_LOGIN] = isFirstTime
        }
    }
    
    // Get the first time login flag
    fun isFirstTimeLogin(): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[FIRST_TIME_LOGIN] ?: true // Default to true
        }
    }
} 