package com.example.taskflowtraining.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflowtraining.databinding.ItemTaskBinding
import com.example.taskflowtraining.model.Task
class TaskAdapter(
    private val onDone: (Task, Boolean) -> Unit,
    private val onDelete: (Task) -> Unit,
    private val onEdit: (Task) -> Unit,
    private val onTaskClick: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {
            override fun areItemsTheSame(old: Task, new: Task) =
                old.id == new.id
            override fun areContentsTheSame(old: Task, new: Task) =
                old == new
        }
    }

    class TaskHolder(val binding: ItemTaskBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int):
            TaskHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TaskHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskHolder, position: Int) {
        val task = getItem(position)
        holder.binding.apply {
            tvTitle.text = task.title
            checkDone.setOnCheckedChangeListener(null)
            checkDone.isChecked = task.isDone
            checkDone.setOnCheckedChangeListener { _, checked ->
                onDone(task, checked)
            }

            tvPriority.text = root.context.getString(com.example.taskflowtraining.R.string.priority_badge, task.priority)
            btnDelete.setOnClickListener { onDelete(task) }

            btnEdit.setOnClickListener { onEdit(task) }

            tvTitle.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

}