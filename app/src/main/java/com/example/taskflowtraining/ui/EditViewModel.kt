package com.example.taskflowtraining.ui

import com.example.taskflowtraining.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskflowtraining.data.TaskRepository
import com.example.taskflowtraining.model.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject


sealed interface EditUiState {

    data object Loading : EditUiState

    data class Content(
        val task: Task
    ) : EditUiState

    data object NotFound : EditUiState

    data class Error(
        val message: UiText
    ) : EditUiState
}

sealed interface EditSaveState {
    data object Idle : EditSaveState
    data object Saving : EditSaveState
    data object Success : EditSaveState
    data class Error(val message: UiText) : EditSaveState
}

@HiltViewModel
class EditViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long =
        savedStateHandle.get<Long>("taskId")
            ?: error("Thiếu taskId")

    private val _saveState =
        MutableStateFlow<EditSaveState>(EditSaveState.Idle)

    val saveState: StateFlow<EditSaveState> =
        _saveState.asStateFlow()

    fun saveTask(newTitle: String, newPriority: Int) {
        if (_saveState.value == EditSaveState.Saving ||
            _saveState.value == EditSaveState.Success
        ) {
            return
        }

        viewModelScope.launch {
            _saveState.value = EditSaveState.Saving

            try {
                repo.updateTitle(
                    id = taskId,
                    newTitle = newTitle,
                    newPriority = newPriority
                )

                _saveState.value = EditSaveState.Success
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _saveState.value = EditSaveState.Error(
                    e.toUiText(R.string.error_save)
                )
            }
        }
    }

    val uiState: StateFlow<EditUiState> =
        repo.observeTaskById(taskId)
            .map { task ->
                if (task == null) {
                    EditUiState.NotFound
                } else {
                    EditUiState.Content(task)
                }
            }
            .catch { exception ->
                emit(
                    EditUiState.Error(
                        exception.toUiText(R.string.error_load)
                    )
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = EditUiState.Loading
            )
}