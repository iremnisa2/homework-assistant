package com.example.homeworkassistant.data.api

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.homeworkassistant.utils.TokenManager
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Custom interceptor to handle premature connection closings
 */
class ConnectionHandlingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return try {
            chain.proceed(request)
        } catch (e: Exception) {
            // Log the error but allow the app to continue
            android.util.Log.e("ConnectionHandling", "Error in connection: ${e.message}", e)
            // Create a fallback response if needed (empty body but success status)
            val mediaType = "application/json".toMediaTypeOrNull()
            val emptyBody = "{}".toResponseBody(mediaType)
            val responseBuilder = okhttp3.Response.Builder()
                .request(request)
                .protocol(okhttp3.Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(emptyBody)
            responseBuilder.build()
        }
    }
}

object ApiClient {
    private const val BASE_URL = "http://10.0.2.2:5000/api/" // Uses 10.0.2.2 for localhost in Android emulator
    
    // Token key for DataStore
    val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    
    fun create(context: Context): ApiService {
        val tokenManager = TokenManager(context)
        
        // Create logging interceptor
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        // Create authentication interceptor
        val authInterceptor = AuthInterceptor(tokenManager)
        
        // Connection handling interceptor
        val connectionHandlingInterceptor = ConnectionHandlingInterceptor()
        
        // Setup OkHttpClient with interceptors
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(connectionHandlingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        
        // Setup date parsing for JSON
        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setLenient() // Add lenient parsing
            .registerTypeAdapter(
                com.example.homeworkassistant.data.models.HomeworkStatus::class.java,
                object : com.google.gson.JsonDeserializer<com.example.homeworkassistant.data.models.HomeworkStatus> {
                    override fun deserialize(
                        json: com.google.gson.JsonElement,
                        typeOfT: java.lang.reflect.Type,
                        context: com.google.gson.JsonDeserializationContext
                    ): com.example.homeworkassistant.data.models.HomeworkStatus {
                        val statusStr = json.asString
                        return com.example.homeworkassistant.data.models.HomeworkStatus.fromString(statusStr)
                    }
                }
            )
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