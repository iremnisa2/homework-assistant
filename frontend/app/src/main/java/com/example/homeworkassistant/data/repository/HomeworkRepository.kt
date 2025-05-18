package com.example.homeworkassistant.data.repository

import android.content.Context
import com.example.homeworkassistant.data.api.ApiClient
import com.example.homeworkassistant.data.api.ApiService
import com.example.homeworkassistant.data.models.Homework
import com.example.homeworkassistant.data.models.HomeworkListResponse
import com.example.homeworkassistant.data.models.HomeworkResponse
import com.example.homeworkassistant.data.models.HomeworkUpdateRequest
import com.example.homeworkassistant.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.io.InputStream

class HomeworkRepository(private val context: Context) {
    private val apiService = ApiClient.create(context)
    
    // Get all homework assignments for the user
    suspend fun getHomeworkList(
        status: String? = null,
        deadlineFilter: String? = null, 
        searchTerm: String? = null
    ): Flow<Resource<List<Homework>>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.getHomeworkList(status, deadlineFilter, searchTerm)
            
            android.util.Log.d("HomeworkRepository", "API Response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val body = response.body()
                android.util.Log.d("HomeworkRepository", "API Response body: $body")
                android.util.Log.d("HomeworkRepository", "API status: ${body?.success}, has data: ${body?.data != null}")
                
                if (body != null && (body.success == true || body.message == "Success") && body.data != null) {
                    android.util.Log.d("HomeworkRepository", "Homework list size: ${body.data.size}")
                    for (homework in body.data) {
                        android.util.Log.d("HomeworkRepository", "Homework: id=${homework.id}, title=${homework.title}, status=${homework.status}")
                    }
                    emit(Resource.Success(body.data))
                } else {
                    val errorMsg = body?.error ?: "Failed to load homework"
                    android.util.Log.e("HomeworkRepository", "API Error: $errorMsg")
                    emit(Resource.Error(errorMsg))
                }
            } else {
                android.util.Log.e("HomeworkRepository", "API request failed with code: ${response.code()}")
                emit(Resource.Error("Failed with code: ${response.code()}"))
            }
        } catch (e: IOException) {
            android.util.Log.e("HomeworkRepository", "Network error: ${e.message}", e)
            emit(Resource.Error("Network error: ${e.message}"))
        } catch (e: Exception) {
            android.util.Log.e("HomeworkRepository", "Unknown error: ${e.message}", e)
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
    
    // Update homework details
    suspend fun updateHomework(
        homeworkId: String,
        title: String?,
        description: String?,
        deadline: String?
    ): Flow<Resource<Homework>> = flow {
        emit(Resource.Loading())
        try {
            val updateRequest = HomeworkUpdateRequest(
                title = title,
                description = description,
                deadline = deadline
            )
            
            val response = apiService.updateHomework(homeworkId, updateRequest)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to update homework"))
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
    
    // Submit homework
    suspend fun submitHomework(homeworkId: String): Flow<Resource<Homework>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.submitHomework(homeworkId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    emit(Resource.Success(body.data))
                } else {
                    emit(Resource.Error(body?.error ?: "Failed to submit homework"))
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
    
    // Download homework file
    suspend fun downloadHomeworkFile(homeworkId: String): Flow<Resource<InputStream>> = flow {
        emit(Resource.Loading())
        try {
            val response = apiService.downloadHomeworkFile(homeworkId)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    emit(Resource.Success(body.byteStream()))
                } else {
                    emit(Resource.Error("Failed to download file"))
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