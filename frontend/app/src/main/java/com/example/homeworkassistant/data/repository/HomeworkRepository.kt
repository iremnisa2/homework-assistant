package com.example.homeworkassistant.data.repository

import android.content.Context
import com.example.homeworkassistant.data.api.ApiClient
import com.example.homeworkassistant.data.api.ApiService
import com.example.homeworkassistant.data.models.Homework
import com.example.homeworkassistant.data.models.HomeworkListResponse
import com.example.homeworkassistant.data.models.HomeworkResponse
import com.example.homeworkassistant.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException

class HomeworkRepository(private val context: Context) {
    private val apiService = ApiClient.create(context)
    
    // Get all homework assignments for the user
    suspend fun getHomeworkList(): Flow<Resource<List<Homework>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHomeworkList()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to load homework"))
                }
            } else {
                emit(Resource.Error("Failed with code: ${response.code()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    
    // Get a specific homework by ID
    suspend fun getHomeworkById(homeworkId: String): Flow<Resource<Homework>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHomeworkById(homeworkId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to load homework details"))
                }
            } else {
                emit(Resource.Error("Failed with code: ${response.code()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    
    // Upload a new homework assignment
    suspend fun uploadHomework(
        title: String,
        description: String,
        deadline: String,
        file: File
    ): Flow<Resource<Homework>> = flow {
        emit(Resource.Loading())
        try {
            // Create request parts
            val titlePart = title.toRequestBody("text/plain".toMediaTypeOrNull())
            val descriptionPart = description.toRequestBody("text/plain".toMediaTypeOrNull())
            val deadlinePart = deadline.toRequestBody("text/plain".toMediaTypeOrNull())
            
            // Create file part
            val fileRequestBody = file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                fileRequestBody
            )
            
            // Make API call
            val response = apiService.uploadHomework(
                titlePart,
                descriptionPart,
                deadlinePart,
                filePart
            )
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to upload homework"))
                }
            } else {
                emit(Resource.Error("Failed with code: ${response.code()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
    
    // Delete homework
    suspend fun deleteHomework(homeworkId: String): Flow<Resource<Boolean>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.deleteHomework(homeworkId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(true))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to delete homework"))
                }
            } else {
                emit(Resource.Error("Failed with code: ${response.code()}"))
            }
        } catch (e: IOException) {
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            emit(Resource.Error("Unknown error: ${e.message}"))
        }
    }
} 