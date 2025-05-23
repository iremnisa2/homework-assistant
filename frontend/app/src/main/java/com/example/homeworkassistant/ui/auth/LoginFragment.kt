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
import com.example.homeworkassistant.MainActivity
import com.example.homeworkassistant.R
import com.example.homeworkassistant.databinding.FragmentLoginBinding
import com.example.homeworkassistant.ui.profile.ProfileActivity
import com.example.homeworkassistant.utils.Resource

class LoginFragment : Fragment() {
    
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: AuthViewModel by activityViewModels()
    private val TAG = "LoginFragment"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }
    
    private fun setupListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()
                viewModel.login(email, password)
            }
        }
        
        // Register link click
        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }
    
    private fun observeViewModel() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            Log.d(TAG, "Login result observed: $result")
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    if (result.data?.isSuccess() == true) {
                        // Success - Show toast
                        Toast.makeText(requireContext(), result.data.message ?: "Login successful!", Toast.LENGTH_SHORT).show()
                        
                        // Let AuthActivity handle the navigation
                        // Navigation will happen through AuthActivity's observer
                    } else {
                        // API returned success=false or status was not "success"
                        // Use errorDetails or message from AuthResponse
                        val errorMessage = result.data?.errorDetails ?: result.data?.message ?: "Login failed"
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
    
    private fun validateInputs(): Boolean {
        var isValid = true
        
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
        
        return isValid
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.btnLogin.isEnabled = true
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
