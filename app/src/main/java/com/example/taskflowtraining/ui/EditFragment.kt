package com.example.taskflowtraining.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.taskflowtraining.R
import com.example.taskflowtraining.databinding.FragmentEditBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditFragment : Fragment(R.layout.fragment_edit) {

    private var _binding: FragmentEditBinding? = null

    private val binding: FragmentEditBinding
        get() = requireNotNull(_binding)

    private val viewModel: EditViewModel by viewModels()

    private var displayedTaskId: Long? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEditBinding.bind(view)

        setupListeners()
        observeUiState()
        observeSaveState()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val priority =
                binding.spinnerPriority.selectedItemPosition + 1

            viewModel.saveTask(title, priority)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.uiState.collect { state ->
                    render(state)
                }
            }
        }
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        EditSaveState.Idle -> {
                            binding.btnSave.isEnabled = true
                        }

                        EditSaveState.Saving -> {
                            binding.btnSave.isEnabled = false
                        }

                        EditSaveState.Success -> {
                            findNavController().navigateUp()
                        }

                        is EditSaveState.Error -> {
                            binding.btnSave.isEnabled = true
                            binding.tvMessage.visibility = View.VISIBLE
                            binding.tvMessage.text = state.message
                        }
                    }
                }
            }
        }
    }

    private fun render(state: EditUiState) = with(binding) {

        progressLoading.visibility = View.GONE
        tvMessage.visibility = View.GONE

        when (state) {

            is EditUiState.Loading -> {
                progressLoading.visibility = View.VISIBLE
            }

            is EditUiState.Content -> {
                if (displayedTaskId != state.task.id) {

                    // Hiển thị tiêu đề công việc
                    etTitle.setText(state.task.title)

                    // Giả sử priority: 1 = Thấp, 2 = Trung bình, 3 = Cao
                    val priorityPosition = when (state.task.priority) {
                        1 -> 0
                        2 -> 1
                        3 -> 2
                        else -> 0
                    }

                    spinnerPriority.setSelection(priorityPosition)

                    displayedTaskId = state.task.id
                }
            }

            is EditUiState.NotFound -> {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = "Không tìm thấy công việc"
            }

            is EditUiState.Error -> {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = state.message
            }
        }
    }

    override fun onDestroyView() {
        displayedTaskId = null
        _binding = null
        super.onDestroyView()
    }
}