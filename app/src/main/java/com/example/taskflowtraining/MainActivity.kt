package com.example.taskflowtraining

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskflowtraining.databinding.ActivityMainBinding
import com.example.taskflowtraining.model.SortMode
import com.example.taskflowtraining.model.Task
import com.example.taskflowtraining.model.TaskFilter
import com.example.taskflowtraining.ui.TaskAdapter
import com.example.taskflowtraining.ui.TaskUiState
import com.example.taskflowtraining.ui.TaskViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val vm: TaskViewModel by viewModels {
        val app = application as TaskFlowApp
        TaskViewModel.TaskViewModelFactory(app.taskRepository, app.settingsRepository)
    }

    private val adapter = TaskAdapter(
        onDone = { task, done -> vm.setDone(task, done) },
        onDelete = { task -> confirmDelete(task) }
    )

    private var currentState = TaskUiState()
    private var renderingSort = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupList()
        setupInputs()
        setupSpinners()
        observeState()
    }

    private fun setupList() {
        binding.recyclerTasks.layoutManager = LinearLayoutManager(this)
        binding.recyclerTasks.adapter = adapter
    }

    private fun setupInputs() = with(binding){
        btnAdd.setOnClickListener {
            val raw = etTitle.text.toString()
            if (raw.isBlank()) {
                etTitle.error = "Nhập tên công việc"
                return@setOnClickListener
            }
            vm.add(raw)
            etTitle.text?.clear()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int,
                                           count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int,
                                       before: Int, count: Int) {
                vm.setQuery(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnImport.setOnClickListener { vm.importSample() }

        btnClear.setOnClickListener {
            AlertDialog.Builder(this@MainActivity)
                .setTitle("Xóa công việc đã hoàn thành?")
                .setNegativeButton("Hủy", null)
                .setPositiveButton("Xóa") { _, _ -> vm.clearCompleted() }
                .show()
        }
        tvFeedback.setOnClickListener { vm.dismissFeedback() }
    }

    private fun setupSpinners() {
        val filters = listOf("Tất cả", "Chưa xong", "Hoàn thành")
        val sorts = listOf("Mới nhất", "Tên A–Z")

        binding.spinnerFilter.adapter = spinnerAdapter(filters)
        binding.spinnerSort.adapter = spinnerAdapter(sorts)

        binding.spinnerFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: android.view.View?, pos: Int, id: Long) {
                    vm.setFilter(TaskFilter.entries[pos])
                }
            }
        binding.spinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) = Unit
                override fun onItemSelected(parent: AdapterView<*>?,
                                            view: android.view.View?, pos: Int, id: Long) {
                    if (!renderingSort) {
                        vm.setSort(SortMode.entries[pos])
                    }
                }
            }

    }

    private fun spinnerAdapter(items: List<String>) =
        ArrayAdapter(this, R.layout.simple_spinner_item, items).apply {
            setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.uiState.collect { state -> render(state) }
            }
        }
    }

    private fun render(state: TaskUiState) = with(binding) {
        currentState = state
        adapter.submitList(state.tasks)
        progressBar.isVisible = state.isLoading
        btnAdd.isEnabled = !state.isLoading
        btnImport.isEnabled = !state.isLoading
        btnClear.isEnabled = !state.isLoading
        tvEmpty.isVisible = state.tasks.isEmpty() && !state.isLoading
        val feedback = state.error ?: state.message
        tvFeedback.isVisible = feedback != null
        tvFeedback.text = feedback.orEmpty()
        val sortIndex = state.sort.ordinal
        if (spinnerSort.selectedItemPosition != sortIndex) {
            renderingSort = true
            spinnerSort.setSelection(sortIndex)
            renderingSort = false
        }
    }

    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(this)
            .setTitle("Xóa công việc?")
            .setMessage(task.title)
            .setNegativeButton("Hủy", null)
            .setPositiveButton("Xóa") { _, _ -> vm.delete(task) }
            .show()
    }
}