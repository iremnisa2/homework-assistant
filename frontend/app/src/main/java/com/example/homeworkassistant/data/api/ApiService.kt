package com.example.homeworkassistant.data.api

import com.example.homeworkassistant.data.models.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // Auth endpoints
    @POST("auth/login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<AuthResponse>
    
    @POST("auth/register")
    suspend fun register(@Body registerRequest: RegisterRequest): Response<AuthResponse>
    
    // Homework endpoints
    @GET("assignments")
    suspend fun getHomeworkList(
        @Query("status") status: String? = null,
        @Query("deadline") deadlineFilter: String? = null,
        @Query("search") searchTerm: String? = null
    ): Response<HomeworkListResponse>
    
    @GET("assignments/{id}")
    suspend fun getHomeworkById(@Path("id") homeworkId: String): Response<HomeworkResponse>
    
    @Multipart
    @POST("assignments")
    suspend fun uploadHomework(
        @Part("title") title: RequestBody,
        @Part("description") description: RequestBody,
        @Part("deadline") deadline: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<HomeworkResponse>
    
    @GET("assignments/{id}/file")
    suspend fun downloadHomeworkFile(@Path("id") homeworkId: String): Response<okhttp3.ResponseBody>
    
    @PUT("assignments/{id}")
    suspend fun updateHomework(
        @Path("id") homeworkId: String,
        @Body updateRequest: HomeworkUpdateRequest
    ): Response<HomeworkResponse>
    
    @PUT("assignments/{id}/submit")
    suspend fun submitHomework(@Path("id") homeworkId: String): Response<HomeworkResponse>
    
    @DELETE("assignments/{id}")
    suspend fun deleteHomework(@Path("id") homeworkId: String): Response<ApiResponse>
    
    // Profile endpoints
    @GET("users/me")
    suspend fun getUserProfile(): Response<UserProfileResponse>
    
    @PUT("users/me")
    suspend fun updateUserProfile(@Body updateProfileRequest: UpdateProfileRequest): Response<UserProfileResponse>
    
    // Generic response for simple operations
    data class ApiResponse(
        val success: Boolean,
        val message: String,
        val error: String?
    )
    
    // User profile models
    data class UserProfileResponse(
        val status: String?,
        val message: String?,
        val data: User?,
        val error: String?
    )
    
    data class UpdateProfileRequest(
        val name: String?,
        val email: String?,
        val settings: Map<String, Any>?,
        val currentPassword: String?,
        val newPassword: String?
    )
} 