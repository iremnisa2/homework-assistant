package com.example.homeworkassistant.data.api

import com.example.homeworkassistant.utils.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth header for login and register requests
        val path = originalRequest.url.encodedPath
        if (path.contains("login") || path.contains("register")) {
            return chain.proceed(originalRequest)
        }
        
        // Get token from TokenManager - use runBlocking because OkHttp interceptor is not a suspend function
        val token = runBlocking {
            tokenManager.getToken().first()
        }
        
        // If no token, proceed with original request
        if (token.isNullOrEmpty()) {
            return chain.proceed(originalRequest)
        }
        
        // Add auth header to request
        val newRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
            
        return chain.proceed(newRequest)
    }
} 