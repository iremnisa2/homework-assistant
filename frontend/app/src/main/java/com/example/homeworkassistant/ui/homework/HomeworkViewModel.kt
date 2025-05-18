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
    
    private val _deleteResult = MutableLiveData<Resource<Boolean>>()
    val deleteResult: LiveData<Resource<Boolean>> = _deleteResult
    
    // Get list of homework assignments
    fun getHomeworkList() {
        viewModelScope.launch {
            homeworkRepository.getHomeworkList().collect { result ->
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
    fun uploadHomework(title: String, description: String, deadline: String, file: java.io.File) {
        viewModelScope.launch {
            homeworkRepository.uploadHomework(title, description, deadline, file).collect { result ->
                _uploadResult.value = result
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