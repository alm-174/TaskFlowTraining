
package com.example.taskflowtraining.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.taskflowtraining.R
import com.example.taskflowtraining.databinding.FragmentDetailBinding
import com.example.taskflowtraining.notification.TaskNotificationHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {

    private var _binding: FragmentDetailBinding? = null

    private val binding: FragmentDetailBinding
        get() = requireNotNull(_binding)

    private val viewModel: DetailViewModel by viewModels()

    private lateinit var notificationHelper: TaskNotificationHelper

    private var pendingReminderTaskId: Long? = null
    private var pendingReminderTaskTitle: String? = null

    // Nhận kết quả xin quyền thông báo
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->

            if (isGranted) {
                val taskId = pendingReminderTaskId
                val taskTitle = pendingReminderTaskTitle

                if (taskId != null && taskTitle != null) {
                    showReminder(taskId, taskTitle)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.notification_denied),
                    Toast.LENGTH_SHORT
                ).show()
            }

            pendingReminderTaskId = null
            pendingReminderTaskTitle = null
        }

    private val photoPickerLauncher =
        registerForActivityResult(
            ActivityResultContracts.PickVisualMedia()
        ) { uri ->

            if (uri != null) {
                binding.ivTaskPhoto.setImageURI(uri)
                checkImageUri(uri)
                binding.ivTaskPhoto.visibility = View.VISIBLE

                Log.d("PHOTO_PICKER", "Ảnh được chọn: $uri")
            } else {
                Log.d("PHOTO_PICKER", "Người dùng đã hủy chọn ảnh")
            }
        }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentDetailBinding.bind(view)

        notificationHelper = TaskNotificationHelper(
            requireContext()
        )

        Log.d(
            "DETAIL_DEBUG",
            "DetailFragment đã được mở"
        )

        setupListeners()
        observeUiState()
    }

    // Quan sát trạng thái từ DetailViewModel
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

    // Hiển thị dữ liệu Task
    private fun render(state: DetailUiState) = with(binding) {

        progressLoading.visibility = View.GONE
        tvTitle.visibility = View.GONE
        tvStatus.visibility = View.GONE
        tvPriority.visibility = View.GONE
        tvMessage.visibility = View.GONE
        btnEdit.visibility = View.GONE
        btnReminder.visibility = View.GONE

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
                btnReminder.visibility = View.VISIBLE

                tvTitle.text = task.title

                tvStatus.text =
                    if (task.isDone) {
                        getString(R.string.done)
                    } else {
                        getString(R.string.not_done)
                    }

                val priorityText = when (task.priority) {
                    1 -> getString(R.string.priority_low)
                    2 -> getString(R.string.priority_medium)
                    3 -> getString(R.string.priority_high)
                    else -> getString(R.string.unknown)
                }

                tvPriority.text = getString(R.string.priority_label, priorityText)
            }

            is DetailUiState.NotFound -> {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = getString(R.string.task_not_found)
            }

            is DetailUiState.Error -> {
                tvMessage.visibility = View.VISIBLE
                tvMessage.text = state.message.resolve(requireContext())
            }
        }
    }

    // Thiết lập sự kiện cho các nút
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

        binding.btnReminder.setOnClickListener {
            val state = viewModel.uiState.value

            if (state is DetailUiState.Content) {
                requestNotificationPermissionIfNeeded(
                    state.task.id,
                    state.task.title
                )
            }
        }

        binding.btnChoosePhoto.setOnClickListener {
            openPhotoPicker()
        }
    }

    // Điều hướng đến màn hình sửa Task
    private fun openEdit(taskId: Long) {
        val args = bundleOf(
            "taskId" to taskId
        )

        findNavController().navigate(
            R.id.action_detail_to_edit,
            args
        )
    }

    // Kiểm tra và xin quyền thông báo khi cần
    private fun requestNotificationPermissionIfNeeded(
        taskId: Long,
        taskTitle: String
    ) {

        // Android 12 trở xuống không cần xin quyền runtime
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            showReminder(taskId, taskTitle)
            return
        }

        val isGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        // Đã có quyền thì gửi thông báo ngay
        if (isGranted) {
            showReminder(taskId, taskTitle)
            return
        }

        // Lưu Task để gửi sau khi người dùng cấp quyền
        pendingReminderTaskId = taskId
        pendingReminderTaskTitle = taskTitle

        notificationPermissionLauncher.launch(
            Manifest.permission.POST_NOTIFICATIONS
        )
    }

    // Gửi thông báo nhắc công việc
    private fun showReminder(
        taskId: Long,
        taskTitle: String
    ) {

        val sent = notificationHelper.showTaskReminder(
            taskId,
            taskTitle
        )

        if (!sent) {
            Toast.makeText(
                requireContext(),
                getString(R.string.notification_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun openPhotoPicker() {
        photoPickerLauncher.launch(
            PickVisualMediaRequest(
                ActivityResultContracts.PickVisualMedia.ImageOnly
            )
        )
    }

    private fun checkImageUri(uri: Uri) {
        try {
            requireContext().contentResolver
                .openInputStream(uri)
                ?.use { inputStream ->
                    Log.d(
                        "PHOTO_PICKER",
                        "Đọc URI thành công"
                    )
                }
        } catch (e: Exception) {
            Log.e(
                "PHOTO_PICKER",
                "Không thể đọc URI",
                e
            )
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
