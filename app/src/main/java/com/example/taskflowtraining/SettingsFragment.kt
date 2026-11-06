package com.example.taskflowtraining

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.example.taskflowtraining.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private var _binding: FragmentSettingsBinding? = null

    private val binding
        get() = requireNotNull(_binding)

    private val handler = Handler(Looper.getMainLooper())

    private val updateStatus = Runnable {
        Log.d("TaskLifecycle", "Callback sau 3 giây")
        binding.tvStatus.text = "Đã xử lý xong!"
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSettingsBinding.bind(view)

        Log.d("TaskLifecycle", "Settings: onViewCreated")

        handler.postDelayed(updateStatus, 3000)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onDestroyView() {
        Log.d("TaskLifecycle", "Settings: onDestroyView")

        handler.removeCallbacks(updateStatus)

        _binding = null

        super.onDestroyView()
    }
}