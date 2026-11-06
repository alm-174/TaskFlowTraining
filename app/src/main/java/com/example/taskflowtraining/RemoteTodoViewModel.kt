
package com.example.taskflowtraining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RemoteTodoUiState(
    val todos: List<Task> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class RemoteTodoViewModel : ViewModel() {

    // Đang sử dụng API thật
    private val repository = RemoteTaskRepository(
        RetrofitRemoteSource(RetrofitClient.todoApi)
    )

    private val _uiState = MutableStateFlow(RemoteTodoUiState())
    val uiState = _uiState.asStateFlow()

    // Quản lý request hiện tại
    private var loadJob: Job? = null

    fun loadTodos() {

        // Nếu đang tải, không tạo request mới
        if (loadJob?.isActive == true) return

        loadJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                when (val result = repository.getTodos()) {

                    is LoadResult.Success -> {
                        _uiState.update {
                            it.copy(
                                todos = result.data.take(10),
                                error = null
                            )
                        }
                    }

                    is LoadResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                error = result.message
                            )
                        }
                    }
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun loadTodoById(input: String) {

        // Ngăn request trùng
        if (loadJob?.isActive == true) return

        val id = input.toIntOrNull()

        if (id == null || id !in 1..200) {
            _uiState.update {
                it.copy(
                    error = "ID phải là số nguyên từ 1 đến 200"
                )
            }
            return
        }

        loadJob = viewModelScope.launch {

            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                when (val result = repository.getTodo(id)) {

                    is LoadResult.Success -> {
                        _uiState.update {
                            it.copy(
                                todos = listOf(result.data),
                                error = null
                            )
                        }
                    }

                    is LoadResult.Failure -> {
                        _uiState.update {
                            it.copy(
                                error = result.message
                            )
                        }
                    }
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}
