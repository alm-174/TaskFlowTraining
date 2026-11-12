
package com.example.taskflowtraining.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.WorkInfo
import com.example.taskflowtraining.databinding.FragmentListBinding
import com.example.taskflowtraining.model.SortMode
import com.example.taskflowtraining.model.Task
import com.example.taskflowtraining.model.TaskFilter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.taskflowtraining.R

@AndroidEntryPoint
class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null

    private val binding: FragmentListBinding
        get() = requireNotNull(_binding)

    private val vm: TaskViewModel by viewModels()

    private val adapter = TaskAdapter(
        onDone = { task, done ->
            vm.setDone(task, done)
        },
        onDelete = { task ->
            confirmDelete(task)
        },
        onEdit = { task ->
            showEditDialog(task)
        },
        onTaskClick = { task ->
            openTaskDetail(task)
        }
    )

    private var currentState = TaskUiState()
    private var renderingControls = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(
            inflater,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupList()
        setupInputs()
        setupSpinners()
        observeState()
    }

    // 1. RecyclerView
    private fun setupList() {
        binding.recyclerTasks.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerTasks.adapter = adapter
        binding.recyclerTasks.itemAnimator = null
    }

    // 2. Các nút và tìm kiếm
    private fun setupInputs() = with(binding) {

        btnAdd.setOnClickListener {
            val raw = etTitle.text.toString()

            if (raw.isBlank()) {
                etTitle.error = getString(R.string.title_required)
                return@setOnClickListener
            }

            val priority =
                spinnerPriority.selectedItemPosition + 1

            vm.add(raw, priority)
            etTitle.text?.clear()
        }

        etSearch.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                if (!renderingControls) {
                    vm.setQuery(s?.toString().orEmpty())
                }
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        btnImport.setOnClickListener {
            vm.importSample()
        }

        btnClear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.clear_confirm))
                .setNegativeButton(getString(R.string.cancel), null)
                .setPositiveButton(getString(R.string.delete)) { _, _ ->
                    vm.clearCompleted()
                }
                .show()
        }

        tvFeedback.setOnClickListener {
            vm.dismissFeedback()
        }
    }

    // 3. Bộ lọc và sắp xếp
    private fun setupSpinners() {

        val filters = listOf(
            getString(R.string.all),
            getString(R.string.active),
            getString(R.string.completed)
        )

        val sorts = listOf(
            getString(R.string.newest),
            getString(R.string.title_sort)
        )

        val priorities = listOf(
            getString(R.string.all),
            getString(R.string.priority_low_option),
            getString(R.string.priority_medium_option),
            getString(R.string.priority_high_option)
        )

        binding.spinnerFilter.adapter =
            spinnerAdapter(filters)

        binding.spinnerSort.adapter =
            spinnerAdapter(sorts)

        binding.spinnerPriorityFilter.adapter =
            spinnerAdapter(priorities)

        // Khôi phục lựa chọn từ state hiện tại
        // trước khi gắn listener.
        val state = vm.uiState.value

        renderingControls = true

        binding.spinnerFilter.setSelection(
            state.filter.ordinal
        )

        binding.spinnerSort.setSelection(
            state.sort.ordinal
        )

        binding.spinnerPriorityFilter.setSelection(
            state.priorityFilter ?: 0
        )

        renderingControls = false

        binding.spinnerFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    if (!renderingControls) {
                        vm.setFilter(TaskFilter.entries[pos])
                    }
                }
            }

        binding.spinnerSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    if (!renderingControls) {
                        vm.setSort(SortMode.entries[pos])
                    }
                }
            }

        binding.spinnerPriorityFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) = Unit

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long
                ) {
                    if (!renderingControls) {
                        val priority =
                            if (pos == 0) null else pos

                        vm.setPriorityFilter(priority)
                    }
                }
            }
    }

    private fun spinnerAdapter(items: List<String>) =
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            items
        ).apply {
            setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
            )
        }

    // 4. Theo dõi StateFlow
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                // 1. Theo dõi danh sách Task và CRUD
                launch {
                    vm.uiState.collect { state ->
                        render(state)
                    }
                }

                // 2. Theo dõi trạng thái Worker
                launch {
                    vm.importWorkInfo.collect { workInfo ->
                        renderImportState(workInfo)
                    }
                }

                // 3. Hiển thị kết quả Import một lần
                launch {
                    vm.importResultToShow.collect { workInfo ->
                        if (workInfo != null) {
                            val message = when (workInfo.state) {
                                WorkInfo.State.SUCCEEDED ->
                                    getString(R.string.import_success)

                                WorkInfo.State.FAILED ->
                                    getString(R.string.import_failed)

                                WorkInfo.State.CANCELLED ->
                                    getString(R.string.import_cancelled)

                                else -> null
                            }

                            if (message != null) {
                                Toast.makeText(
                                    requireContext(),
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                vm.acknowledgeImportResult(workInfo.id)
                            }
                        }
                    }
                }
            }
        }
    }


    private fun renderImportState(workInfo: WorkInfo?) {
        val isImportActive = workInfo != null &&
                !workInfo.state.isFinished

        binding.btnImport.isEnabled =
            !currentState.isLoading && !isImportActive

        when (workInfo?.state) {
            WorkInfo.State.ENQUEUED -> {
                binding.btnImport.text = getString(R.string.import_waiting)
            }

            WorkInfo.State.RUNNING -> {
                binding.btnImport.text = getString(R.string.import_running)
            }

            WorkInfo.State.SUCCEEDED -> {
                binding.btnImport.text = getString(R.string.import_action)
            }

            WorkInfo.State.FAILED -> {
                binding.btnImport.text = getString(R.string.import_action)
            }

            WorkInfo.State.CANCELLED -> {
                binding.btnImport.text = getString(R.string.import_action)
            }

            else -> {
                binding.btnImport.text = getString(R.string.import_action)
            }
        }
    }

    // 5. Hiển thị dữ liệu
    private fun render(state: TaskUiState) = with(binding) {

        currentState = state

        adapter.submitList(state.tasks)

        progressBar.isVisible = state.isLoading

        btnAdd.isEnabled = !state.isLoading

        btnImport.isEnabled =
            !state.isLoading &&
                    vm.importWorkInfo.value?.state?.isFinished != false

        btnClear.isEnabled = !state.isLoading

        tvEmpty.isVisible =
            state.tasks.isEmpty() && !state.isLoading

        val feedback = state.error ?: state.message

        tvFeedback.isVisible = feedback != null
        tvFeedback.text = feedback?.resolve(requireContext()).orEmpty()

        renderingControls = true

        val filterIndex = state.filter.ordinal
        if (spinnerFilter.selectedItemPosition != filterIndex) {
            spinnerFilter.setSelection(filterIndex)
        }

        val sortIndex = state.sort.ordinal
        if (spinnerSort.selectedItemPosition != sortIndex) {
            spinnerSort.setSelection(sortIndex)
        }

        val priorityIndex = state.priorityFilter ?: 0
        if (spinnerPriorityFilter.selectedItemPosition != priorityIndex) {
            spinnerPriorityFilter.setSelection(priorityIndex)
        }

        val query = state.query
        if (etSearch.text.toString() != query) {
            etSearch.setText(query)
            etSearch.setSelection(query.length)
        }

        renderingControls = false
    }

    // 6. Xác nhận xóa Task
    private fun confirmDelete(task: Task) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.delete_confirm))
            .setMessage(task.title)
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.delete)) { _, _ ->
                vm.delete(task)
            }
            .show()
    }

    // 7. Dialog sửa Task (tạm giữ chức năng cũ)
    private fun showEditDialog(task: Task) {

        val input = EditText(requireContext()).apply {
            setText(task.title)
            selectAll()
        }

        val prioritySpinner = Spinner(requireContext()).apply {

            val priorities = listOf(
                getString(R.string.priority_low_option),
                getString(R.string.priority_medium_option),
                getString(R.string.priority_high_option)
            )

            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                priorities
            ).apply {
                setDropDownViewResource(
                    android.R.layout.simple_spinner_dropdown_item
                )
            }

            setSelection(
                (task.priority - 1).coerceIn(0, 2)
            )
        }

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL

            val padding =
                (20 * resources.displayMetrics.density).toInt()

            setPadding(
                padding,
                padding,
                padding,
                padding
            )

            addView(input)
            addView(prioritySpinner)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.edit_task))
            .setView(layout)
            .setNegativeButton(
                android.R.string.cancel,
                null
            )
            .setPositiveButton(getString(R.string.save), null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {

                    val newTitle =
                        input.text.toString().trim()

                    val newPriority =
                        prioritySpinner.selectedItemPosition + 1

                    when {
                        newTitle.isBlank() -> {
                            input.error = getString(R.string.title_empty)
                        }

                        newTitle.length > 100 -> {
                            input.error = getString(R.string.title_too_long)
                        }

                        else -> {
                            vm.updateTask(
                                task,
                                newTitle,
                                newPriority
                            )
                            dialog.dismiss()
                        }
                    }
                }
        }

        dialog.show()
    }

    private fun openTaskDetail(task : Task){
        val args = bundleOf(
            "taskId" to task.id
        )

        findNavController().navigate(
            R.id.action_list_to_detail,
            args
        )
    }

    override fun onDestroyView() {
        binding.recyclerTasks.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
