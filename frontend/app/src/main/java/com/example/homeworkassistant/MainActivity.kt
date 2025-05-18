package com.example.homeworkassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.homeworkassistant.databinding.ActivityMainBinding
import com.example.homeworkassistant.ui.auth.AuthActivity
import com.example.homeworkassistant.ui.homework.AddHomeworkActivity
import com.example.homeworkassistant.ui.homework.HomeworkAdapter
import com.example.homeworkassistant.ui.homework.HomeworkDetailsActivity
import com.example.homeworkassistant.ui.homework.HomeworkViewModel
import com.example.homeworkassistant.ui.profile.ProfileActivity
import com.example.homeworkassistant.utils.Resource

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var homeworkAdapter: HomeworkAdapter
    private val viewModel: HomeworkViewModel by viewModels()
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Check if coming from login
        val fromLogin = intent.getBooleanExtra("FROM_LOGIN", false)
        if (fromLogin) {
            // We've successfully logged in and redirected here
            Log.d(TAG, "MainActivity launched from login")
            Toast.makeText(this, "Welcome to your homework list", Toast.LENGTH_SHORT).show()
        }
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        
        // Setup RecyclerView
        setupRecyclerView()
        
        // Setup FAB
        binding.fabAddHomework.setOnClickListener {
            startActivity(Intent(this, AddHomeworkActivity::class.java))
        }
        
        // Load homework list
        viewModel.getHomeworkList()
        
        // Observe homework list changes
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        homeworkAdapter = HomeworkAdapter { homework ->
            // Handle homework item click (view details)
            val intent = Intent(this, HomeworkDetailsActivity::class.java).apply {
                putExtra("HOMEWORK_ID", homework.id)
            }
            startActivity(intent)
        }
        
        binding.recyclerViewHomework.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = homeworkAdapter
        }
    }
    
    private fun observeViewModel() {
        viewModel.homeworkList.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    val homeworkList = result.data
                    if (homeworkList.isNullOrEmpty()) {
                        showEmptyState()
                    } else {
                        hideEmptyState()
                        homeworkAdapter.submitList(homeworkList)
                    }
                }
                is Resource.Error -> {
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
                is Resource.Loading -> {
                    showLoading()
                }
            }
        })
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.recyclerViewHomework.visibility = View.GONE
        binding.tvEmptyState.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.recyclerViewHomework.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        binding.tvEmptyState.visibility = View.VISIBLE
        binding.recyclerViewHomework.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.tvEmptyState.visibility = View.GONE
        binding.recyclerViewHomework.visibility = View.VISIBLE
    }
    
    // Menu handling
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_profile -> {
                startActivity(Intent(this, ProfileActivity::class.java))
                true
            }
            R.id.action_logout -> {
                viewModel.logout()
                startActivity(Intent(this, AuthActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}