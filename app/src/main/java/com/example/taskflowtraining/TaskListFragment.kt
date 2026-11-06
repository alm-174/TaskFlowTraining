
package com.example.taskflowtraining

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.taskflowtraining.databinding.FragmentTaskListBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TaskListFragment : Fragment(R.layout.fragment_task_list) {

    private var _binding: FragmentTaskListBinding? = null

    private val binding: FragmentTaskListBinding
        get() = requireNotNull(_binding)

    private val viewModel: TaskViewModel by viewModels {
        val app = requireActivity().application as TaskFlowApp

        TaskViewModelFactory(
            app.taskRepository,
            app.settingsRepository
        )
    }

    private lateinit var adapter: TaskAdapter

    private val priorities = listOf(
        TaskPriority.LOW,
        TaskPriority.MEDIUM,
        TaskPriority.HIGH
    )

    private val sortModes = listOf(
        SortMode.DEFAULT,
        SortMode.TITLE_ASC,
        SortMode.TITLE_DESC,
        SortMode.PRIORITY_HIGH_FIRST
    )

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentTaskListBinding.bind(view)

        Log.d("TaskLifecycle", "TaskList: onViewCreated")

        // 1. Nút Refresh API
        binding.btnRefreshRemote.setOnClickListener {
            viewModel.refreshRemote()
        }

        // 2. Thống kê dữ liệu Room
        val dao = (requireActivity().application as TaskFlowApp)
            .database.taskDao()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                combine(
                    dao.observeTaskCount(),
                    dao.observeCompletedCount()
                ) { total, completed ->
                    total to completed
                }.collect { (total, completed) ->

                    binding.tvTotalTasks.text =
                        "Tổng công việc: $total"

                    binding.tvCompletedTasks.text =
                        "Đã hoàn thành: $completed"

                    val percent =
                        if (total == 0) 0
                        else completed * 100 / total

                    binding.progressTasks.progress = percent
                }
            }
        }

        // 3. Khởi tạo Adapter
        adapter = TaskAdapter(
            onToggle = { id ->
                viewModel.toggle(id)
            },
            onDelete = { id ->
                viewModel.delete(id)
            }
        )

        binding.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())

        binding.recyclerView.adapter = adapter

        // 4. Tìm kiếm
        binding.etSearch.setText(
            viewModel.uiState.value.searchQuery
        )

        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(
                text?.toString().orEmpty()
            )
        }

        // 5. Spinner Priority
        val priorityLabels = listOf(
            "Thấp",
            "Trung bình",
            "Cao"
        )

        val priorityAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorityLabels
        )

        priorityAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spPriority.adapter = priorityAdapter
        binding.spPriority.setSelection(1)

        // 6. Thêm công việc
        binding.btnAdd.setOnClickListener {
            val title = binding.etTitle.text.toString()

            val selectedPriority =
                priorities[binding.spPriority.selectedItemPosition]

            viewModel.add(title, selectedPriority)

            if (title.isNotBlank()) {
                binding.etTitle.text.clear()
            }
        }

        // 7. Bộ lọc
        binding.btnAll.setOnClickListener {
            viewModel.setFilter(TaskFilter.ALL)
        }

        binding.btnPending.setOnClickListener {
            viewModel.setFilter(TaskFilter.PENDING)
        }

        binding.btnDone.setOnClickListener {
            viewModel.setFilter(TaskFilter.DONE)
        }


        // 8. Spinner Sort + DataStore
        val sortLabels = listOf(
            "Mặc định",
            "Tên A → Z",
            "Tên Z → A",
            "Ưu tiên cao trước"
        )

        val sortAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortLabels
        )

        sortAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        binding.spSort.adapter = sortAdapter

        // Chưa cho Spinner ghi vào DataStore
        // cho đến khi đã đồng bộ giá trị ban đầu
        var spinnerReady = false

        binding.spSort.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (!spinnerReady) return

                    // Chỉ ghi khi người dùng chọn giá trị khác
                    // giá trị đang có trong ViewModel
                    val selectedMode = sortModes[position]

                    if (selectedMode != viewModel.currentSortMode.value) {
                        viewModel.setSortMode(selectedMode)
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }

        // Đọc SortMode đã lưu và cập nhật Spinner
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                combine(
                    viewModel.currentSortMode,
                    viewModel.sortModeLoaded
                ) { mode, loaded ->
                    mode to loaded
                }.collect { (mode, loaded) ->

                    if (!loaded) return@collect

                    val position = sortModes.indexOf(mode)

                    if (position >= 0) {
                        if (!spinnerReady) {
                            // Đồng bộ lựa chọn ban đầu trước khi
                            // bật callback lưu cài đặt
                            binding.spSort.setSelection(position)
                            spinnerReady = true
                        } else if (
                            binding.spSort.selectedItemPosition != position
                        ) {
                            binding.spSort.setSelection(position)
                        }
                    }
                }
            }
        }

        // 9. Mở SettingsFragment
        binding.btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    SettingsFragment()
                )
                .addToBackStack(null)
                .commit()
        }

        // 10. Quan sát UI State
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.uiState.collect { state ->

                    renderTasks(state)

                    state.error?.let { message ->
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_SHORT
                        ).show()

                        viewModel.clearError()
                    }
                }
            }
        }
    }

    // 11. Hiển thị danh sách và trạng thái
    private fun renderTasks(state: TaskUiState) {

        binding.btnAdd.isEnabled = !state.isLoading
        binding.btnAdd.text =
            if (state.isLoading) "Đang xử lý..."
            else "Thêm"

        binding.btnRefreshRemote.isEnabled = !state.isLoading
        binding.btnRefreshRemote.text =
            if (state.isLoading) "Đang tải..."
            else "Refresh API"

        adapter.submitList(state.tasks)

        binding.tvEmpty.visibility =
            if (state.tasks.isEmpty()) View.VISIBLE
            else View.GONE

        val completed = state.tasks.count { it.isDone }

        binding.tvStats.text =
            "Đang hiển thị: ${state.tasks.size} | Hoàn thành: $completed"
    }

    // 12. Giải phóng ViewBinding
    override fun onDestroyView() {

        Log.d("TaskLifecycle", "TaskList: onDestroyView")

        binding.recyclerView.adapter = null
        _binding = null

        super.onDestroyView()
    }
}
