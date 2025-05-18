package com.example.homeworkassistant.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.homeworkassistant.MainActivity
import com.example.homeworkassistant.databinding.ActivityProfileBinding
import com.example.homeworkassistant.utils.Resource
import com.example.homeworkassistant.utils.SessionManager

class ProfileActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Load user data
        viewModel.getUserProfile()
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Save profile button click
        binding.btnSaveProfile.setOnClickListener {
            if (validateInputs()) {
                val name = binding.etFullName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val settingsStr = binding.etSettings.text.toString().trim()
                val currentPassword = binding.etCurrentPassword.text.toString().trim()
                val newPassword = binding.etNewPassword.text.toString().trim()
                
                // Convert settings string to Map<String, Any>
                // This is a simplified conversion; a robust solution would use a JSON library like Gson
                val settingsMap: Map<String, Any>? = if (settingsStr.isNotEmpty()) {
                    try {
                        // Basic parsing for a flat JSON string like {"key1":"value1", "key2":true}
                        // Not robust for nested JSON or complex types.
                        settingsStr.removeSurrounding("{", "}").split(",").associate {
                            val (key, value) = it.split(":")
                            key.trim().removeSurrounding("\"") to value.trim().removeSurrounding("\"")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this, "Invalid settings format. Use JSON.", Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                } else {
                    null
                }
                
                viewModel.updateProfile(
                    name = name,
                    email = email,
                    settings = settingsMap,
                    currentPassword = currentPassword,
                    newPassword = if (newPassword.isNotEmpty()) newPassword else null
                )
            }
        }
        
        // Skip button click - go directly to main activity
        binding.btnSkip.setOnClickListener {
            navigateToMainActivity()
        }
        
        // Profile image upload (in a real app, this would handle selecting and uploading an image)
        binding.fabEditProfile.setOnClickListener {
            Toast.makeText(this, "Profile picture upload not implemented in this version", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        Log.d("ProfileActivity", "observeViewModel() called") 
        // Observe profile data
        viewModel.profileData.observe(this, Observer { result ->
            Log.d("ProfileActivity", "profileData observer triggered. Result is: $result") 
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    result.data?.let { user ->
                        Log.d("ProfileActivity", "User data received: FullName='${user.fullName}', Email='${user.email}'")

                        // Ensure views are visible just in case
                        binding.tilFullName.visibility = View.VISIBLE
                        binding.etFullName.visibility = View.VISIBLE
                        binding.tilEmail.visibility = View.VISIBLE
                        binding.etEmail.visibility = View.VISIBLE
                        
                        binding.etFullName.setText(user.fullName)
                        binding.etEmail.setText(user.email)
                        binding.etSettings.setText(user.settings?.toString() ?: "")

                        Log.d("ProfileActivity", "etFullName after setText: '${binding.etFullName.text}', Visible: ${binding.etFullName.visibility == View.VISIBLE}, IsShown: ${binding.etFullName.isShown}")
                        Log.d("ProfileActivity", "etEmail after setText: '${binding.etEmail.text}', Visible: ${binding.etEmail.visibility == View.VISIBLE}, IsShown: ${binding.etEmail.isShown}")
                        Log.d("ProfileActivity", "tilFullName Visible: ${binding.tilFullName.visibility == View.VISIBLE}, IsShown: ${binding.tilFullName.isShown}")
                        Log.d("ProfileActivity", "tilEmail Visible: ${binding.tilEmail.visibility == View.VISIBLE}, IsShown: ${binding.tilEmail.isShown}")

                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
        
        // Observe profile update
        viewModel.updateResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    navigateToMainActivity()
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate name
        if (binding.etFullName.text.toString().trim().isEmpty()) {
            binding.tilFullName.error = "Name is required"
            isValid = false
        } else {
            binding.tilFullName.error = null
        }
        
        // Validate email
        if (binding.etEmail.text.toString().trim().isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(binding.etEmail.text.toString().trim()).matches()) {
            binding.tilEmail.error = "Enter a valid email"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Validate current password
        if (binding.etCurrentPassword.text.toString().trim().isEmpty()) {
            binding.tilCurrentPassword.error = "Current password is required"
            isValid = false
        } else {
            binding.tilCurrentPassword.error = null
        }
        
        // New password is optional - no validation needed
        
        return isValid
    }
    
    private fun navigateToMainActivity() {
        // Set first time login flag to false
        val sessionManager = SessionManager(this)
        sessionManager.setFirstTimeLogin(false)
        
        // Navigate to MainActivity
        Intent(this, MainActivity::class.java).also {
            startActivity(it)
            finish()
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnSaveProfile.isEnabled = false
        binding.btnSkip.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnSaveProfile.isEnabled = true
        binding.btnSkip.isEnabled = true
    }
} 