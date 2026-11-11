package com.example.taskflowtraining.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskflowtraining.data.TaskRepository
import com.example.taskflowtraining.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Content(
        val task: Task
    ) : DetailUiState

    data object NotFound : DetailUiState

    data class Error(
        val message: String
    ) : DetailUiState
}


@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long =
        savedStateHandle.get<Long>("taskId")
            ?: error("Thiếu taskId")

    val uiState: StateFlow<DetailUiState> =
        repo.observeTaskById(taskId)
            .map { task ->
                if (task == null) {
                    DetailUiState.NotFound
                } else {
                    DetailUiState.Content(task)
                }
            }
            .catch { exception ->
                emit(
                    DetailUiState.Error(
                        exception.message ?: "Không thể tải công việc"
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DetailUiState.Loading
            )
}