package com.example.homeworkassistant.ui.homework

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.homeworkassistant.databinding.ActivityAddHomeworkBinding
import com.example.homeworkassistant.utils.Resource
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddHomeworkActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddHomeworkBinding
    private val viewModel: HomeworkViewModel by viewModels()
    private var selectedFileUri: Uri? = null
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Result launcher for file picking
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                updateSelectedFileInfo(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddHomeworkBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Add Homework"

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, monthOfYear)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        binding.btnPickDate.setOnClickListener {
            DatePickerDialog(
                this@AddHomeworkActivity,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.btnAddFile.setOnClickListener {
            openFilePicker()
        }

        binding.btnSaveHomework.setOnClickListener {
            saveHomework()
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*" // All file types
        }
        filePickerLauncher.launch(intent)
    }

    private fun updateSelectedFileInfo(uri: Uri) {
        // Query the file name
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                
                val fileName = it.getString(nameIndex)
                val fileSize = it.getLong(sizeIndex)
                
                // Update UI
                binding.tvSelectedFile.text = "Selected: $fileName (${formatFileSize(fileSize)})"
                binding.tvSelectedFile.visibility = View.VISIBLE
            }
        }
    }

    private fun formatFileSize(size: Long): String {
        val kb = size / 1024.0
        val mb = kb / 1024.0
        
        return when {
            mb >= 1.0 -> String.format("%.2f MB", mb)
            kb >= 1.0 -> String.format("%.2f KB", kb)
            else -> "$size bytes"
        }
    }

    private fun updateDateInView() {
        binding.textViewDueDate.text = dateFormat.format(calendar.time)
    }

    private fun saveHomework() {
        val title = binding.editTextHomeworkTitle.text.toString().trim()
        val description = binding.editTextHomeworkDescription.text.toString().trim()
        val dueDate = binding.textViewDueDate.text.toString().trim()

        // Validate inputs
        if (title.isEmpty()) {
            binding.editTextHomeworkTitle.error = "Title cannot be empty"
            binding.editTextHomeworkTitle.requestFocus()
            return
        }

        if (dueDate.isEmpty() || dueDate == "Select Due Date") {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedFileUri == null) {
            Toast.makeText(this, "Please select a file", Toast.LENGTH_SHORT).show()
            return
        }

        // Convert Uri to File
        val file = uriToFile(selectedFileUri!!)
        if (file != null) {
            // Show loading
            binding.progressBar.visibility = View.VISIBLE
            binding.btnSaveHomework.isEnabled = false
            
            // Upload homework
            viewModel.uploadHomework(title, description, dueDate, file)
        } else {
            Toast.makeText(this, "Failed to process file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToFile(uri: Uri): File? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val fileName = getFileName(uri)
            val file = File(cacheDir, fileName)
            
            inputStream?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "homework_file"
        
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        }
        
        return fileName
    }

    private fun observeViewModel() {
        viewModel.uploadResult.observe(this, Observer { result ->
            when (result) {
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveHomework.isEnabled = true
                    
                    Toast.makeText(this, "Homework saved successfully", Toast.LENGTH_SHORT).show()
                    
                    // Redirect to MainActivity (homeworks page) instead of just finishing
                    val intent = Intent(this, com.example.homeworkassistant.MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSaveHomework.isEnabled = true
                    
                    Toast.makeText(this, result.message, Toast.LENGTH_LONG).show()
                }
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSaveHomework.isEnabled = false
                }
            }
        })
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