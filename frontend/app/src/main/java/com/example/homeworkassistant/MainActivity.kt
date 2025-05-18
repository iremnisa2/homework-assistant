package com.example.homeworkassistant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.SearchView
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
    
    // Filters
    private var currentStatusFilter: String? = null
    private var currentDeadlineFilter: String? = null
    private var currentSearchTerm: String? = null
    
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
        
        // Setup filter chips
        setupFilterChips()
        
        // Setup search view
        setupSearchView()
        
        // Load homework list
        loadHomeworkList()
        
        // Observe homework list changes
        observeViewModel()
    }
    
    private fun setupFilterChips() {
        // Status filter chips
        binding.chipDraft.setOnClickListener {
            currentStatusFilter = if (binding.chipDraft.isChecked) "DRAFT" else null
            updateFilters()
        }
        
        binding.chipSubmitted.setOnClickListener {
            currentStatusFilter = if (binding.chipSubmitted.isChecked) "SUBMITTED" else null
            updateFilters()
        }
        
        binding.chipCompleted.setOnClickListener {
            currentStatusFilter = if (binding.chipCompleted.isChecked) "COMPLETED" else null
            updateFilters()
        }
        
        // Deadline filter chips
        binding.chipPastDue.setOnClickListener {
            currentDeadlineFilter = if (binding.chipPastDue.isChecked) "before" else null
            updateFilters()
        }
        
        binding.chipToday.setOnClickListener {
            currentDeadlineFilter = if (binding.chipToday.isChecked) "today" else null
            updateFilters()
        }
        
        binding.chipUpcoming.setOnClickListener {
            currentDeadlineFilter = if (binding.chipUpcoming.isChecked) "after" else null
            updateFilters()
        }
        
        // Clear all filters button
        binding.btnClearFilters.setOnClickListener {
            clearAllFilters()
        }
    }
    
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchTerm = query?.takeIf { it.isNotBlank() }
                updateFilters()
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    currentSearchTerm = null
                    updateFilters()
                }
                return true
            }
        })
        
        binding.searchView.setOnCloseListener {
            currentSearchTerm = null
            updateFilters()
            false
        }
    }
    
    private fun clearAllFilters() {
        // Clear status chips
        binding.chipDraft.isChecked = false
        binding.chipSubmitted.isChecked = false
        binding.chipCompleted.isChecked = false
        
        // Clear deadline chips
        binding.chipPastDue.isChecked = false
        binding.chipToday.isChecked = false
        binding.chipUpcoming.isChecked = false
        
        // Clear search
        binding.searchView.setQuery("", false)
        binding.searchView.clearFocus()
        
        // Reset filter variables
        currentStatusFilter = null
        currentDeadlineFilter = null
        currentSearchTerm = null
        
        // Load unfiltered list
        loadHomeworkList()
    }
    
    private fun updateFilters() {
        // Apply filters to the list
        loadHomeworkList()
    }
    
    private fun loadHomeworkList() {
        viewModel.getHomeworkList(currentStatusFilter, currentDeadlineFilter, currentSearchTerm)
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
                    Log.d(TAG, "Received Success result")
                    hideLoading()
                    val homeworkList = result.data
                    Log.d(TAG, "Homework list received: ${homeworkList?.size ?: 0} items")
                    
                    if (homeworkList.isNullOrEmpty()) {
                        Log.d(TAG, "Empty homework list, showing empty state")
                        showEmptyState()
                    } else {
                        Log.d(TAG, "Updating adapter with homework items")
                        hideEmptyState()
                        homeworkAdapter.submitList(homeworkList)
                        Log.d(TAG, "Adapter items: ${homeworkAdapter.itemCount}")
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "Error loading homework list: ${result.message}")
                    hideLoading()
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                    showEmptyState()
                }
                is Resource.Loading -> {
                    Log.d(TAG, "Loading homework list...")
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
    
    override fun onResume() {
        super.onResume()
        // Reload the homework list when returning to this activity
        loadHomeworkList()
    }
}