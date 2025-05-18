package com.example.homeworkassistant.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.homeworkassistant.R
import com.example.homeworkassistant.databinding.FragmentRegisterBinding
import com.example.homeworkassistant.ui.profile.ProfileActivity
import com.example.homeworkassistant.utils.Resource

class RegisterFragment : Fragment() {
    
    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by activityViewModels()
    private val TAG = "RegisterFragment"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Register button click
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                val name = binding.etName.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.register(name, email, password)
            }
        }
        
        // Login link click
        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }
    
    private fun observeViewModel() {
        viewModel.registerResult.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "Register result observed: $result")
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    if (result.data?.isSuccess() == true) {
                        // Success - Show toast and handle navigation
                        Toast.makeText(requireContext(), result.data.message ?: "Registration successful!", Toast.LENGTH_SHORT).show()
                        
                        // Get the auth data
                        val authData = result.data.data
                        if (authData != null) {
                            // After registration, always navigate to Profile
                            Log.d(TAG, "Registration successful with data: ${authData.user}")
                            navigateToProfile()
                        } else {
                            // Should not happen if isSuccess() is true and API guarantees data object on success
                            Log.w(TAG, "Registration reported success, but AuthData is null.")
                            // Optionally, still navigate to profile or show a generic success/error
                            navigateToProfile() // Defaulting to profile navigation
                        }
                    } else {
                        // API returned non-success status or data was null
                        // Use errorDetails or message from AuthResponse
                        val errorMessage = result.data?.errorDetails ?: result.data?.message ?: "Registration failed"
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(requireContext(), result.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        }
    }
    
    private fun navigateToProfile() {
        Log.d(TAG, "Navigating to ProfileActivity")
        Intent(requireContext(), ProfileActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(it)
            requireActivity().finish()
        }
    }
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
        // Validate name
        if (binding.etName.text.toString().trim().isEmpty()) {
            binding.tilName.error = "Name is required"
            isValid = false
        } else {
            binding.tilName.error = null
        }
        
        // Validate email
        if (binding.etEmail.text.toString().trim().isEmpty()) {
            binding.tilEmail.error = "Email is required"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }
        
        // Validate password
        if (binding.etPassword.text.toString().trim().isEmpty()) {
            binding.tilPassword.error = "Password is required"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }
        
        // Validate confirm password
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.error = "Confirm password is required"
            isValid = false
        } else if (confirmPassword != password) {
            binding.tilConfirmPassword.error = "Passwords do not match"
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }
        
        return isValid
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnRegister.isEnabled = true
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 