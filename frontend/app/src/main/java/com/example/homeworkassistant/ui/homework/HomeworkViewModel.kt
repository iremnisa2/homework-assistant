package com.example.homeworkassistant.ui.homework

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.homeworkassistant.data.models.Homework
import com.example.homeworkassistant.data.repository.AuthRepository
import com.example.homeworkassistant.data.repository.HomeworkRepository
import com.example.homeworkassistant.utils.Resource
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream

class HomeworkViewModel(application: Application) : AndroidViewModel(application) {
    
    private val context = application.applicationContext
    private val homeworkRepository = HomeworkRepository(context)
    private val authRepository = AuthRepository(context)
    
    private val _homeworkList = MutableLiveData<Resource<List<Homework>>>()
    val homeworkList: LiveData<Resource<List<Homework>>> = _homeworkList
    
    private val _homeworkDetails = MutableLiveData<Resource<Homework>>()
    val homeworkDetails: LiveData<Resource<Homework>> = _homeworkDetails
    
    private val _uploadResult = MutableLiveData<Resource<Homework>>()
    val uploadResult: LiveData<Resource<Homework>> = _uploadResult
    
    private val _updateResult = MutableLiveData<Resource<Homework>>()
    val updateResult: LiveData<Resource<Homework>> = _updateResult
    
    private val _submitResult = MutableLiveData<Resource<Homework>>()
    val submitResult: LiveData<Resource<Homework>> = _submitResult
    
    private val _downloadResult = MutableLiveData<Resource<InputStream>>()
    val downloadResult: LiveData<Resource<InputStream>> = _downloadResult
    
    private val _deleteResult = MutableLiveData<Resource<Boolean>>()
    val deleteResult: LiveData<Resource<Boolean>> = _deleteResult
    
    // Get list of homework assignments
    fun getHomeworkList(status: String? = null, deadlineFilter: String? = null, searchTerm: String? = null) {
        viewModelScope.launch {
            homeworkRepository.getHomeworkList(status, deadlineFilter, searchTerm).collect { result ->
                _homeworkList.value = result
            }
        }
    }
    
    // Get homework details by ID
    fun getHomeworkById(homeworkId: String) {
        viewModelScope.launch {
            homeworkRepository.getHomeworkById(homeworkId).collect { result ->
                _homeworkDetails.value = result
            }
        }
    }
    
    // Upload a new homework assignment
    fun uploadHomework(title: String, description: String, deadline: String, file: File) {
        viewModelScope.launch {
            homeworkRepository.uploadHomework(title, description, deadline, file).collect { result ->
                _uploadResult.value = result
            }
        }
    }
    
    // Update homework details
    fun updateHomework(homeworkId: String, title: String?, description: String?, deadline: String?) {
        viewModelScope.launch {
            homeworkRepository.updateHomework(homeworkId, title, description, deadline).collect { result ->
                _updateResult.value = result
            }
        }
    }
    
    // Submit homework
    fun submitHomework(homeworkId: String) {
        viewModelScope.launch {
            homeworkRepository.submitHomework(homeworkId).collect { result ->
                _submitResult.value = result
            }
        }
    }
    
    // Download homework file
    fun downloadHomeworkFile(homeworkId: String) {
        viewModelScope.launch {
            homeworkRepository.downloadHomeworkFile(homeworkId).collect { result ->
                _downloadResult.value = result
            }
        }
    }
    
    // Delete a homework assignment
    fun deleteHomework(homeworkId: String) {
        viewModelScope.launch {
            homeworkRepository.deleteHomework(homeworkId).collect { result ->
                _deleteResult.value = result
            }
        }
    }
    
    // Logout user
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
} 