package com.example.homeworkassistant.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.homeworkassistant.MainActivity
import com.example.homeworkassistant.R
import com.example.homeworkassistant.databinding.ActivityAuthBinding
import com.example.homeworkassistant.ui.profile.ProfileActivity
import com.example.homeworkassistant.utils.Resource

class AuthActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityAuthBinding
    private lateinit var navController: NavController
    private val viewModel: AuthViewModel by viewModels()
    private val TAG = "AuthActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        Log.d(TAG, "AuthActivity onCreate called")
        
        // Setup navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        setupObservers()
        
        // FORCE-CHECK: Explicitly check login status after a short delay
        android.os.Handler(mainLooper).postDelayed({
            Log.d(TAG, "Executing force login check")
            viewModel.checkLoginStatus()
        }, 1000)
        
        // ALTERNATİF ÇÖZÜM: Global exception handler ekle
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable -> 
            Log.e(TAG, "Uncaught exception: ${throwable.message}", throwable)
            try {
                // Yönlendirme hatası durumunda son çare olarak MainActivity'ye geçiş
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra("FROM_ERROR_HANDLER", true)
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Log.e(TAG, "Error in exception handler: ${e.message}", e)
            }
        }
    }
    
    private fun setupObservers() {
        // Check if already logged in
        viewModel.isLoggedIn.observe(this) { isLoggedIn ->
            Log.d(TAG, "isLoggedIn changed: $isLoggedIn")
            if (isLoggedIn) {
                // User is already logged in, navigate to MainActivity
                Log.d(TAG, "isLoggedIn is true, calling navigateToMain()")
                navigateToMain()
            }
        }
        
        // Login result observer
        viewModel.loginResult.observe(this) { result ->
            Log.d(TAG, "Login result: $result")
            
            // Use isSuccess() from AuthResponse (result.data is AuthResponse)
            if (result is Resource.Success && result.data?.isSuccess() == true) { 
                val responsePayload = result.data // This is AuthResponse
                val authDetails = responsePayload.data // This is AuthData?
                
                // Immediate navigation without delay
                Log.d(TAG, "Executing immediate navigation after login success check")
                if (authDetails != null) {
                    Log.d(TAG, "Login successful with authData, isFirstLogin: ${authDetails.isFirstLogin}")
                    // Handle authDetails.isFirstLogin (which is Boolean?)
                    if (authDetails.isFirstLogin == true) { // Explicit check for true as it's nullable
                        navigateToProfile()
                    } else {
                        // Also navigate to main if isFirstLogin is null (i.e., not specified, assume not first login)
                        navigateToMain()
                    }
                } else {
                    // Data object within successful response is null, default to main view.
                    Log.d(TAG, "Login successful, but authDetails (AuthData) is null. Defaulting to navigateToMain.")
                    navigateToMain()
                }
            }
            // No explicit else here to handle Resource.Error or Resource.Loading for navigation
        }
        
        // Register result observer
        viewModel.registerResult.observe(this) { result ->
            Log.d(TAG, "Register result: $result")
            // Use isSuccess() from AuthResponse and ensure data.data (AuthData) is not null
            if (result is Resource.Success && result.data?.isSuccess() == true && result.data?.data != null) {
                Log.d(TAG, "Registration successful, navigating to profile")
                navigateToProfile()
            }
        }
    }
    
    private fun navigateToMain() {
        try {
            Log.d(TAG, "Navigating to MainActivity (Homework View)")
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                          Intent.FLAG_ACTIVITY_CLEAR_TASK or
                          Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.putExtra("FROM_LOGIN", true)
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to MainActivity: ${e.message}", e)
            
            // Fallback approach using explicit component name
            try {
                val fallbackIntent = Intent()
                fallbackIntent.setClassName(
                    packageName,
                    "com.example.homeworkassistant.MainActivity"
                )
                fallbackIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                                      Intent.FLAG_ACTIVITY_CLEAR_TASK or
                                      Intent.FLAG_ACTIVITY_CLEAR_TOP
                fallbackIntent.putExtra("FROM_LOGIN", true)
                startActivity(fallbackIntent)
                finish()
            } catch (e2: Exception) {
                Log.e(TAG, "Fallback navigation failed: ${e2.message}", e2)
                Toast.makeText(this, "Navigation error, please restart the app", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun navigateToProfile() {
        try {
            Log.d(TAG, "Attempting to navigate to ProfileActivity in AuthActivity")
            val intent = Intent(this, ProfileActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error navigating to ProfileActivity: ${e.message}", e)
            Toast.makeText(this, "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
} 