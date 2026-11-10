package com.example.taskflowtraining

import android.R
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val vm: TaskViewModel by viewModels()

    private val adapter = TaskAdapter(
        onDone = { task, done -> vm.setDone(task, done) },
        onDelete = { task -> confirmDelete(task) },
        onEdit = {task -> showEditDialog(task)}
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
        binding.recyclerTasks.itemAnimator = null
    }

    private fun setupInputs() = with(binding){
        btnAdd.setOnClickListener {
            val raw = etTitle.text.toString()
            if (raw.isBlank()) {
                etTitle.error = "Nhập tên công việc"
                return@setOnClickListener
            }
            val priority = spinnerPriority.selectedItemPosition + 1

            vm.add(raw, priority)
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
        val priorities = listOf( "Tất cả", "Thấp (1)", "Trung bình (2)", "Cao (3)")

        binding.spinnerFilter.adapter = spinnerAdapter(filters)
        binding.spinnerSort.adapter = spinnerAdapter(sorts)
        binding.spinnerPriorityFilter.adapter = spinnerAdapter(priorities)

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

        binding.spinnerPriorityFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: android.view.View?,
                pos: Int,
                id: Long
            ) {
                val priority = if (pos == 0) null else pos

                vm.setPriorityFilter(priority)
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


    private fun showEditDialog(task: Task) {


        val input = EditText(this).apply {
            setText(task.title)
            selectAll()
        }

        val prioritySpinner = Spinner(this).apply {

            val priorities = listOf(
                "Thấp (1)",
                "Trung bình (2)",
                "Cao (3)"
            )

            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_item,
                priorities
            ).apply {
                setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }

            setSelection((task.priority - 1).coerceIn(0, 2))
        }


        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            val padding = (20 * resources.displayMetrics.density).toInt()
            setPadding(padding, padding, padding, padding)

            addView(input)
            addView(prioritySpinner)
        }


        val dialog = AlertDialog.Builder(this)
            .setTitle("Sửa công việc")
            .setView(layout)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton("Lưu", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val newTitle = input.text.toString()
                    val newPriority =
                        prioritySpinner.selectedItemPosition + 1

                    if (newTitle.isBlank()) {
                        input.error = "Không để trống tên"
                    } else if (newTitle.trim().length > 100) {
                        input.error = "Tên tối đa 100 ký tự"
                    } else {
                        vm.updateTask(task, newTitle, newPriority)
                        dialog.dismiss()
                    }
                }
        }

        dialog.show()
    }

}