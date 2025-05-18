package com.example.homeworkassistant.ui.profile

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeworkassistant.data.api.ApiService
import com.example.homeworkassistant.data.models.User
import com.example.homeworkassistant.utils.Resource
import kotlinx.coroutines.launch

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    
    // Repository would be better, but for simplicity, we'll use API directly
    private val apiClient = com.example.homeworkassistant.data.api.ApiClient.create(context)
    
    private val _profileData = MutableLiveData<Resource<User>>()
    val profileData: LiveData<Resource<User>> = _profileData
    
    private val _updateResult = MutableLiveData<Resource<User>>()
    val updateResult: LiveData<Resource<User>> = _updateResult
    
    // Get user profile from API
    fun getUserProfile() {
        viewModelScope.launch {
            _profileData.value = Resource.Loading()
            
            try {
                val response = apiClient.getUserProfile()
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ProfileViewModel", "getUserProfile - Response successful. Body: $body")
                    if (body != null) {
                        Log.d("ProfileViewModel", "getUserProfile - Body not null. body.status: '${body.status}', body.data exists: ${body.data != null}")
                    }

                    if (body != null && body.status.equals("success", ignoreCase = true) && body.data != null) {
                        _profileData.value = Resource.Success(body.data)
                    } else {
                        val errorReason = "Body: $body, body.status: ${body?.status}, body.data == null: ${body?.data == null}"
                        Log.e("ProfileViewModel", "getUserProfile - Condition for Success failed. Reason: $errorReason")
                        val errorMessage = body?.error ?: body?.message ?: "Failed to load profile (unexpected response structure)"
                        Log.e("ProfileViewModel", "getUserProfile - Setting Resource.Error with message: $errorMessage")
                        _profileData.value = Resource.Error(errorMessage)
                    }
                } else {
                    Log.e("ProfileViewModel", "getUserProfile - Response not successful. Code: ${response.code()}, Message: ${response.message()}")
                    _profileData.value = Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "getUserProfile - Exception: ${e.message}", e)
                _profileData.value = Resource.Error("Error: ${e.message}")
            }
        }
    }
    
    // Update user profile
    fun updateProfile(name: String, email: String, settings: Map<String, Any>?, currentPassword: String, newPassword: String? = null) {
        viewModelScope.launch {
            _updateResult.value = Resource.Loading()
            
            try {
                val updateRequest = ApiService.UpdateProfileRequest(
                    name = name,
                    email = email,
                    settings = settings,
                    currentPassword = currentPassword,
                    newPassword = newPassword
                )
                
                val response = apiClient.updateUserProfile(updateRequest)
                
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("ProfileViewModel", "updateProfile - Response successful. Body: $body")
                    if (body != null) {
                        Log.d("ProfileViewModel", "updateProfile - Body not null. body.status: '${body.status}', body.data exists: ${body.data != null}")
                    }

                    if (body != null && body.status.equals("success", ignoreCase = true) && body.data != null) {
                        _updateResult.value = Resource.Success(body.data)
                    } else {
                        val errorReason = "Body: $body, body.status: ${body?.status}, body.data == null: ${body?.data == null}"
                        Log.e("ProfileViewModel", "updateProfile - Condition for Success failed. Reason: $errorReason")
                        val errorMessage = body?.error ?: body?.message ?: "Failed to update profile (unexpected response structure)"
                        Log.e("ProfileViewModel", "updateProfile - Setting Resource.Error with message: $errorMessage")
                        _updateResult.value = Resource.Error(errorMessage)
                    }
                } else {
                    Log.e("ProfileViewModel", "updateProfile - Response not successful. Code: ${response.code()}, Message: ${response.message()}")
                    _updateResult.value = Resource.Error("Error ${response.code()}: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ProfileViewModel", "updateProfile - Exception: ${e.message}", e)
                _updateResult.value = Resource.Error("Error: ${e.message}")
            }
        }
    }
} 