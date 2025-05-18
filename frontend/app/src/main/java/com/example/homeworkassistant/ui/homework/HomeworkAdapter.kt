package com.example.homeworkassistant.ui.homework

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworkassistant.data.models.Homework
import com.example.homeworkassistant.databinding.ItemHomeworkBinding
import java.text.SimpleDateFormat
import java.util.Locale

class HomeworkAdapter(private val onItemClick: (Homework) -> Unit) :
    ListAdapter<Homework, HomeworkAdapter.HomeworkViewHolder>(HomeworkDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeworkViewHolder {
        val binding = ItemHomeworkBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HomeworkViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: HomeworkViewHolder, position: Int) {
        val homework = getItem(position)
        holder.bind(homework)
    }
    
    inner class HomeworkViewHolder(private val binding: ItemHomeworkBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val homework = getItem(position)
                    onItemClick(homework)
                }
            }
        }
        
        fun bind(homework: Homework) {
            binding.tvTitle.text = homework.title
            
            // Format date
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            binding.tvDeadline.text = "Due: ${dateFormat.format(homework.deadline)}"
            
            // Set status text and color
            binding.tvStatus.text = homework.status.name
            
            // Show if there's a plagiarism report
            binding.tvPlagiarismScore.apply {
                if (homework.plagiarismReport != null) {
                    text = "Similarity: ${homework.plagiarismReport.similarityPercentage}%"
                    visibility = android.view.View.VISIBLE
                } else {
                    visibility = android.view.View.GONE
                }
            }
        }
    }
    
    private class HomeworkDiffCallback : DiffUtil.ItemCallback<Homework>() {
        override fun areItemsTheSame(oldItem: Homework, newItem: Homework): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: Homework, newItem: Homework): Boolean {
            return oldItem == newItem
        }
    }
} 