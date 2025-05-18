package com.example.homeworkassistant.data.repository

import android.content.Context
import com.example.homeworkassistant.data.api.ApiClient
import com.example.homeworkassistant.data.models.AuthResponse
import com.example.homeworkassistant.data.models.LoginRequest
import com.example.homeworkassistant.data.models.RegisterRequest
import com.example.homeworkassistant.utils.Resource
import com.example.homeworkassistant.utils.TokenManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import java.io.IOException

class AuthRepository(private val context: Context) {
    private val apiService = ApiClient.create(context)
    private val tokenManager = TokenManager(context)
    
    // Login user
    suspend fun login(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val loginRequest = LoginRequest(email, password)
            val response = apiService.login(loginRequest)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.isSuccess() && body.data != null) {
                        // Log token details (safely)
                        val token = body.data.token
                        val tokenPreview = if (token.length > 10) "${token.substring(0, 10)}..." else "invalid_token"
                        android.util.Log.d("AuthRepository", "Got token: $tokenPreview, length: ${token.length}")
                        
                        // Save auth token
                        tokenManager.saveToken(token)
                        android.util.Log.d("AuthRepository", "Token saved successfully")
                        
                        // Force log the login status after token save
                        val isLoggedIn = tokenManager.isLoggedIn().first()
                        android.util.Log.d("AuthRepository", "After token save, isLoggedIn: $isLoggedIn")
                        
                        emit(Resource.Success(body))
                    } else {
                        emit(Resource.Error(body.message ?: body.errorDetails ?: "Login failed"))
                    }
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string() ?: "Login failed with code: ${response.code()}"
                    emit(Resource.Error(errorBody))
                } catch (e: Exception) {
                    emit(Resource.Error("Login failed with code: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    
    // Register user
    suspend fun register(name: String, email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        emit(Resource.Loading())
        try {
            val registerRequest = RegisterRequest(fullName = name, email, password)
            val response = apiService.register(registerRequest)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    if (body.isSuccess() && body.data != null) {
                        // Save auth token
                        tokenManager.saveToken(body.data.token)
                        emit(Resource.Success(body))
                    } else {
                        emit(Resource.Error(body.message ?: body.errorDetails ?: "Registration failed"))
                    }
                } else {
                    emit(Resource.Error("Empty response from server"))
                }
            } else {
                try {
                    val errorBody = response.errorBody()?.string() ?: "Registration failed with code: ${response.code()}"
                    emit(Resource.Error(errorBody))
                } catch (e: Exception) {
                    emit(Resource.Error("Registration failed with code: ${response.code()}"))
                }
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    
    // Logout user
    suspend fun logout() {
        tokenManager.clearToken()
    }
    
    // Check if user is logged in
    fun isUserLoggedIn() = tokenManager.isLoggedIn()
}
 