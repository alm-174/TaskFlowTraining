package com.example.taskflowtraining

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.taskflowtraining.databinding.ItemTaskBinding

class TaskAdapter(
    private val onToggle: (Long) -> Unit,
    private val onDelete: (Long) -> Unit
) : ListAdapter<Task, TaskAdapter.VH>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Task>() {

            override fun areItemsTheSame(
                a: Task,
                b: Task
            ): Boolean = a.id == b.id

            override fun areContentsTheSame(
                a: Task,
                b: Task
            ): Boolean = a == b
        }
    }

    inner class VH(
        val b: ItemTaskBinding
    ) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(
        p: ViewGroup,
        type: Int
    ): VH {
        return VH(
            ItemTaskBinding.inflate(
                LayoutInflater.from(p.context),
                p,
                false
            )
        )
    }

    override fun onBindViewHolder(
        h: VH,
        pos: Int
    ) {
        val item = getItem(pos)

        h.b.tvTitle.text = item.title

        h.b.cbDone.setOnCheckedChangeListener(null)
        h.b.cbDone.isChecked = item.isDone

        h.b.cbDone.setOnCheckedChangeListener { _, checked ->
            if (checked != item.isDone) {
                onToggle(item.id)
            }
        }

        h.b.btnDelete.setOnClickListener {
            onDelete(item.id)
        }
    }
}