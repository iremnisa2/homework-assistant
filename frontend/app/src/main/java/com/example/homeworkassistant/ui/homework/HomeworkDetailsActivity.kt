package com.example.homeworkassistant.ui.homework

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.example.homeworkassistant.R
import com.example.homeworkassistant.databinding.ActivityHomeworkDetailsBinding
import com.example.homeworkassistant.utils.Resource
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeworkDetailsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityHomeworkDetailsBinding
    private val viewModel: HomeworkViewModel by viewModels()
    private var homeworkId: String? = null
    private var isEditing = false
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeworkDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Homework Details"
        
        // Get homework ID from intent
        homeworkId = intent.getStringExtra("HOMEWORK_ID")
        if (homeworkId == null) {
            Toast.makeText(this, "Invalid homework ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        // Setup buttons
        setupButtons()
        
        // Observe homework details
        observeViewModel()
        
        // Load homework details
        viewModel.getHomeworkById(homeworkId!!)
    }
    
    private fun setupButtons() {
        // Edit button
        binding.btnEdit.setOnClickListener {
            if (isEditing) {
                // Save changes
                saveChanges()
            } else {
                // Switch to edit mode
                enableEditMode()
            }
        }
        
        // Submit button
        binding.btnSubmit.setOnClickListener {
            submitHomework()
        }
        
        // Download button
        binding.btnDownload.setOnClickListener {
            downloadHomeworkFile()
        }
        
        // Delete button
        binding.btnDelete.setOnClickListener {
            confirmDelete()
        }
        
        // Date picker
        binding.etDueDate.setOnClickListener {
            if (isEditing) {
                showDatePicker()
            }
        }
    }
    
    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }
        
        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
    
    private fun updateDateInView() {
        binding.etDueDate.setText(dateFormat.format(calendar.time))
    }
    
    private fun enableEditMode() {
        isEditing = true
        binding.btnEdit.text = "Save"
        
        // Enable editing of fields
        binding.etTitle.isEnabled = true
        binding.etDescription.isEnabled = true
        binding.etDueDate.isEnabled = true
    }
    
    private fun disableEditMode() {
        isEditing = false
        binding.btnEdit.text = "Edit"
        
        // Disable editing of fields
        binding.etTitle.isEnabled = false
        binding.etDescription.isEnabled = false
        binding.etDueDate.isEnabled = false
    }
    
    private fun saveChanges() {
        val title = binding.etTitle.text.toString().trim()
        val description = binding.etDescription.text.toString().trim()
        val dueDate = binding.etDueDate.text.toString().trim()
        
        if (title.isEmpty()) {
            binding.etTitle.error = "Title cannot be empty"
            return
        }
        
        // Call update API
        viewModel.updateHomework(homeworkId!!, title, description, dueDate)
    }
    
    private fun submitHomework() {
        AlertDialog.Builder(this)
            .setTitle("Submit Homework")
            .setMessage("Are you sure you want to submit this homework? You won't be able to edit it after submission.")
            .setPositiveButton("Submit") { _, _ ->
                viewModel.submitHomework(homeworkId!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun downloadHomeworkFile() {
        viewModel.downloadHomeworkFile(homeworkId!!)
    }
    
    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Delete Homework")
            .setMessage("Are you sure you want to delete this homework? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteHomework(homeworkId!!)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun observeViewModel() {
        // Observe homework details
        viewModel.homeworkDetails.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    val homework = result.data
                    if (homework != null) {
                        populateHomeworkDetails(homework)
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
        
        // Observe update result
        viewModel.updateResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    disableEditMode()
                    Toast.makeText(this, "Homework updated successfully", Toast.LENGTH_SHORT).show()
                    // Refresh details
                    viewModel.getHomeworkById(homeworkId!!)
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
        
        // Observe submit result
        viewModel.submitResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Homework submitted successfully", Toast.LENGTH_SHORT).show()
                    // Refresh details
                    viewModel.getHomeworkById(homeworkId!!)
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
        
        // Observe download result
        viewModel.downloadResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    val inputStream = result.data
                    if (inputStream != null) {
                        saveAndOpenFile(inputStream, "homework_file")
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
        
        // Observe delete result
        viewModel.deleteResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    hideLoading()
                    Toast.makeText(this, "Homework deleted successfully", Toast.LENGTH_SHORT).show()
                    finish() // Go back to the list
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
    
    private fun populateHomeworkDetails(homework: com.example.homeworkassistant.data.models.Homework) {
        binding.etTitle.setText(homework.title)
        binding.etDescription.setText(homework.description)
        binding.etDueDate.setText(dateFormat.format(homework.deadline))
        
        // Set status
        binding.tvStatus.text = "Status: ${homework.status}"
        
        // Show/hide buttons based on status
        if (homework.status == com.example.homeworkassistant.data.models.HomeworkStatus.SUBMITTED ||
            homework.status == com.example.homeworkassistant.data.models.HomeworkStatus.ANALYZED ||
            homework.status == com.example.homeworkassistant.data.models.HomeworkStatus.COMPLETED) {
            // If already submitted, don't allow editing or submission
            binding.btnEdit.visibility = View.GONE
            binding.btnSubmit.visibility = View.GONE
        } else {
            binding.btnEdit.visibility = View.VISIBLE
            binding.btnSubmit.visibility = View.VISIBLE
        }
        
        // Display plagiarism report if available
        homework.plagiarismReport?.let { report ->
            binding.cardPlagiarismReport.visibility = View.VISIBLE
            binding.tvPlagiarismScore.text = "Similarity: ${report.similarityPercentage}%"
            // Add more details as needed
        } ?: run {
            binding.cardPlagiarismReport.visibility = View.GONE
        }
        
        // Display grammar report if available
        homework.grammarReport?.let { report ->
            binding.cardGrammarReport.visibility = View.VISIBLE
            binding.tvClarityScore.text = "Clarity: ${report.clarityScore}"
            binding.tvStructureScore.text = "Structure: ${report.structureScore}"
            binding.tvReadabilityScore.text = "Readability: ${report.readabilityScore}"
            // Add more details as needed
        } ?: run {
            binding.cardGrammarReport.visibility = View.GONE
        }
        
        // Display instructor feedback if available
        if (!homework.instructorFeedback.isNullOrEmpty()) {
            binding.cardFeedback.visibility = View.VISIBLE
            binding.tvFeedback.text = homework.instructorFeedback
        } else {
            binding.cardFeedback.visibility = View.GONE
        }
    }
    
    private fun saveAndOpenFile(inputStream: java.io.InputStream, fileName: String) {
        try {
            // Create a temporary file
            val fileExtension = ".pdf" // Default extension, should be determined based on actual file type
            val file = File(cacheDir, "$fileName$fileExtension")
            val outputStream = FileOutputStream(file)
            
            // Copy input stream to output stream
            inputStream.copyTo(outputStream)
            outputStream.close()
            inputStream.close()
            
            // Open the file with an appropriate app
            val uri = FileProvider.getUriForFile(
                this,
                "com.example.homeworkassistant.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/octet-stream")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            if (intent.resolveActivity(packageManager) != null) {
                startActivity(intent)
            } else {
                Toast.makeText(this, "No app found to open this file", Toast.LENGTH_SHORT).show()
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "Error opening file: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.contentLayout.visibility = View.GONE
    }
    
    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
        binding.contentLayout.visibility = View.VISIBLE
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_details, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 