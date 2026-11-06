
package com.example.taskflowtraining

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.example.taskflowtraining.databinding.ActivityRemoteTodoBinding

class RemoteTodoActivity : ComponentActivity() {

    private lateinit var binding: ActivityRemoteTodoBinding

    private val viewModel: RemoteTodoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRemoteTodoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoadTodos.setOnClickListener {
            viewModel.loadTodos()
        }


        binding.btnFindTodo.setOnClickListener {

            val idInput = binding.edtTodoId.text.toString().trim()

            viewModel.loadTodoById(idInput)
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->

                    binding.progressBar.visibility =
                        if (state.isLoading) View.VISIBLE else View.GONE

                    binding.btnLoadTodos.isEnabled = !state.isLoading

                    binding.tvStatus.text = when {
                        state.isLoading -> "Đang tải dữ liệu..."
                        state.error != null -> state.error
                        state.todos.isEmpty() -> "Chưa có dữ liệu"
                        else -> "Đã tải ${state.todos.size} Todo"
                    }

                    binding.tvTodos.text = state.todos.joinToString(
                        separator = "\n\n"
                    ) { task ->
                        val status =
                            if (task.isDone) "Hoàn thành" else "Chưa hoàn thành"

                        "ID: ${task.id}\n${task.title}\n$status"
                    }
                }
            }
        }
    }
}
