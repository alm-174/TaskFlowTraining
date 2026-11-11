package com.example.taskflowtraining.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.taskflowtraining.databinding.FragmentDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.example.taskflowtraining.R
import androidx.navigation.fragment.findNavController

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var _binding: FragmentDetailBinding? = null

    private val binding: FragmentDetailBinding
        get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels()



    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDetailBinding.bind(view)

        Log.d("DETAIL_DEBUG", "DetailFragment đã được mở")

        setupListeners()
        observeUiState()
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

    private fun render(state: DetailUiState) = with(binding){

        progressLoading.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvStatus.visibility = View.GONE
        tvPriority.visibility = View.GONE
        tvMessage.visibility = View.GONE
        btnEdit.visibility = View.GONE

        when (state) {
            is DetailUiState.Loading -> {
                progressLoading.visibility = View.VISIBLE
            }

            is DetailUiState.Content -> {
                val task = state.task

                tvTitle.visibility = View.VISIBLE
                tvStatus.visibility = View.VISIBLE
                tvPriority.visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE

                tvTitle.text = task.title

                tvStatus.text =
                    if (task.isDone) {
                        "Đã hoàn thành"
                    } else {
                        "Chưa hoàn thành"
                    }

                val priorityText = when (task.priority) {
                    1 -> "Thấp"
                    2 -> "Trung bình"
                    3 -> "Cao"
                    else -> "Không xác định"
                }

                tvPriority.text = "Độ ưu tiên: $priorityText"
            }

            is DetailUiState.NotFound -> {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = "Không tìm thấy công việc"
            }

            is DetailUiState.Error -> {
                tvMessage.visibility = View.VISIBLE
                binding.tvMessage.text = state.message
            }
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEdit.setOnClickListener {
            val state = viewModel.uiState.value

            if (state is DetailUiState.Content) {
                openEdit(state.task.id)
            }
        }
    }

    private fun openEdit(taskId: Long) {
        val args = bundleOf(
            "taskId" to taskId
        )

        findNavController().navigate(
            R.id.action_detail_to_edit,
            args
        )
    }


    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}