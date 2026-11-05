package com.example.taskflowtraining

import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskflowtraining.databinding.FragmentTaskListBinding

class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null

    private val binding
        get() = requireNotNull(_binding)

    private val tasks = mutableListOf<Task>()
    private var nextId = 1L

    private var searchQuery = ""
    private var currentFilter = TaskFilter.ALL

    private lateinit var adapter: TaskAdapter

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentTaskListBinding.bind(view)

        adapter = TaskAdapter(
            onToggle = { id -> toggleTask(id) },
            onDelete = { id -> deleteTask(id) }
        )

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            searchQuery = text?.toString().orEmpty()
            renderTasks()
        }

        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val error = validateTitle(title)

            if (error != null) {
                binding.etTitle.error = error
                return@setOnClickListener
            }

            addTask(title)
            binding.etTitle.text.clear()
        }

        binding.btnAll.setOnClickListener {
            currentFilter = TaskFilter.ALL
            renderTasks()
        }

        binding.btnPending.setOnClickListener {
            currentFilter = TaskFilter.PENDING
            renderTasks()
        }

        binding.btnDone.setOnClickListener {
            currentFilter = TaskFilter.DONE
            renderTasks()
        }

        renderTasks()
    }

    private fun validateTitle(raw: String): String? {
        if (raw.isBlank()) {
            return "Tên công việc không được để trống"
        }
        return null
    }

    private fun addTask(raw: String) {
        if (validateTitle(raw) != null) return

        tasks += Task(
            id = nextId++,
            title = raw.trim()
        )

        renderTasks()
    }

    private fun toggleTask(id: Long) {
        val i = tasks.indexOfFirst { it.id == id }

        if (i >= 0) {
            tasks[i] = tasks[i].copy(
                isDone = !tasks[i].isDone
            )
        }

        renderTasks()
    }

    private fun deleteTask(id: Long) {
        tasks.removeAll { it.id == id }
        renderTasks()
    }

    private fun renderTasks() {
        val result = tasks.filter { task ->

            val matchesSearch = task.title.contains(
                searchQuery,
                ignoreCase = true
            )

            val matchesFilter = when (currentFilter) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.isDone
                TaskFilter.DONE -> task.isDone
            }

            matchesSearch && matchesFilter
        }

        adapter.submitList(result)

        binding.tvStats.text =
            "Tổng: ${tasks.size} | Đã xong: ${tasks.count { it.isDone }}"

        binding.tvEmpty.text =
            if (tasks.isEmpty()) {
                "Chưa có công việc nào"
            } else {
                "Không có công việc phù hợp"
            }

        binding.tvEmpty.visibility =
            if (result.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        _binding = null
        super.onDestroyView()
    }
}