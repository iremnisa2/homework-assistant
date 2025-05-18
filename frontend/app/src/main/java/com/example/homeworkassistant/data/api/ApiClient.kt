package com.example.homeworkassistant.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.homeworkassistant.utils.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000/api/" // Uses 10.0.2.2 for localhost in Android emulator
    
    // Token key for DataStore
    val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    
    fun create(context: Context): ApiService {
        val tokenManager = TokenManager(context)
        
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.HEADERS
        }
        
        // Create authentication interceptor
        val authInterceptor = AuthInterceptor(tokenManager)
        
        // Setup OkHttpClient with interceptors
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
        
        // Setup date parsing for JSON
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
        
        // Create Retrofit instance
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
} 