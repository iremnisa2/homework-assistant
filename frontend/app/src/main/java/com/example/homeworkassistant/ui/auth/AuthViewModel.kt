package com.example.homeworkassistant.ui.auth

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeworkassistant.data.models.AuthData
import com.example.homeworkassistant.data.models.AuthResponse
import com.example.homeworkassistant.data.repository.AuthRepository
import com.example.homeworkassistant.utils.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    private val TAG = "AuthViewModel"
    
    private val _loginResult = MutableLiveData<Resource<AuthResponse>>()
    val loginResult: LiveData<Resource<AuthResponse>> = _loginResult
    
    private val _registerResult = MutableLiveData<Resource<AuthResponse>>()
    val registerResult: LiveData<Resource<AuthResponse>> = _registerResult
    
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    init {
        checkLoginStatus()
    }
    
    // Check if user is logged in
    fun checkLoginStatus() {
        viewModelScope.launch {
            try {
                authRepository.isUserLoggedIn().collect { isLoggedIn ->
                    Log.d(TAG, "User login status: $isLoggedIn")
                    _isLoggedIn.postValue(isLoggedIn)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking login status: ${e.message}")
                _isLoggedIn.postValue(false)
            }
        }
    }
    
    // Login user
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.login(email, password).collect { result ->
                    Log.d(TAG, "Login result: $result")
                    when (result) {
                        is Resource.Success -> {
                            _loginResult.postValue(Resource.Success(result.data!!))
                            // Update isLoggedIn status immediately on successful login
                            if (result.data.isSuccess() && result.data.data != null) {
                                _isLoggedIn.postValue(true)
                            }
                        }
                        is Resource.Error -> {
                            _loginResult.postValue(Resource.Error(result.message ?: "Unknown error"))
                        }
                        is Resource.Loading -> {
                            _loginResult.postValue(Resource.Loading())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Login error: ${e.message}")
                _loginResult.postValue(Resource.Error("Login failed: ${e.message}"))
            }
        }
    }
    
    // Register user
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            try {
                authRepository.register(name, email, password).collect { result ->
                    Log.d(TAG, "Register result: $result")
                    when (result) {
                        is Resource.Success -> {
                            _registerResult.postValue(Resource.Success(result.data!!))
                            // Manually update isLoggedIn status
                            if (result.data.isSuccess() && result.data.data != null) {
                                _isLoggedIn.postValue(true)
                            }
                        }
                        is Resource.Error -> {
                            _registerResult.postValue(Resource.Error(result.message ?: "Unknown error"))
                        }
                        is Resource.Loading -> {
                            _registerResult.postValue(Resource.Loading())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Register error: ${e.message}")
                _registerResult.postValue(Resource.Error("Registration failed: ${e.message}"))
            }
        }
    }
    
    // Logout user
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _isLoggedIn.postValue(false)
        }
    }
} 