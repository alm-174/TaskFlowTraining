
package com.example.taskflowtraining

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repo: TaskRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TaskUiState())
    val uiState: StateFlow<TaskUiState> = _uiState.asStateFlow()

    // Tìm kiếm, lọc và sắp xếp
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)

    private val sortMode = MutableStateFlow(SortMode.DEFAULT)
    val currentSortMode: StateFlow<SortMode> =
        sortMode.asStateFlow()

    // Đánh dấu DataStore đã được đọc
    private val _sortModeLoaded = MutableStateFlow(false)
    val sortModeLoaded: StateFlow<Boolean> =
        _sortModeLoaded.asStateFlow()

    // Danh sách Task được lọc và sắp xếp
    @OptIn(kotlinx.coroutines.FlowPreview::class)
    val visibleTasks: StateFlow<List<Task>> = combine(
        repo.observeTasks(),
        query.debounce(250),
        filter,
        sortMode
    ) { tasks, q, f, sort ->

        val filtered = tasks.filter { task ->
            task.title.contains(
                q.trim(),
                ignoreCase = true
            ) && when (f) {
                TaskFilter.ALL -> true
                TaskFilter.PENDING -> !task.isDone
                TaskFilter.DONE -> task.isDone
            }
        }

        when (sort) {
            SortMode.DEFAULT -> filtered

            SortMode.TITLE_ASC ->
                filtered.sortedBy {
                    it.title.lowercase()
                }

            SortMode.TITLE_DESC ->
                filtered.sortedByDescending {
                    it.title.lowercase()
                }

            SortMode.PRIORITY_HIGH_FIRST ->
                filtered.sortedByDescending {
                    when (it.priority) {
                        TaskPriority.HIGH -> 3
                        TaskPriority.MEDIUM -> 2
                        TaskPriority.LOW -> 1
                    }
                }
        }

    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    init {
        // 1. Quan sát danh sách Task từ Room
        viewModelScope.launch {
            visibleTasks.collect { tasks ->
                _uiState.update {
                    it.copy(tasks = tasks)
                }
            }
        }

        // 2. Khôi phục SortMode từ DataStore
        viewModelScope.launch {
            settingsRepository.sortMode.collect { savedMode ->
                sortMode.value = savedMode
                _sortModeLoaded.value = true
            }
        }
    }

    fun setSearchQuery(text: String) {
        query.value = text

        _uiState.update {
            it.copy(searchQuery = text)
        }
    }

    fun setFilter(mode: TaskFilter) {
        filter.value = mode

        _uiState.update {
            it.copy(currentFilter = mode)
        }
    }

    // Chỉ lưu khi DataStore đã khôi phục xong
    fun setSortMode(mode: SortMode) {
        if (!_sortModeLoaded.value) return
        if (sortMode.value == mode) return

        sortMode.value = mode

        viewModelScope.launch {
            settingsRepository.setSortMode(mode)
        }
    }

    fun add(
        title: String,
        priority: TaskPriority = TaskPriority.MEDIUM
    ) {
        if (title.isBlank()) {
            _uiState.update {
                it.copy(
                    error = "Vui lòng nhập tên công việc"
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                repo.add(title.trim(), priority)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = e.message
                            ?: "Không thể thêm công việc"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun toggle(id: Long) {
        viewModelScope.launch {
            repo.toggle(id)
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch {
            repo.delete(id)
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    fun refreshRemote() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null
                )
            }

            try {
                val insertedCount = repo.refreshRemote()

                android.util.Log.d(
                    "DAY14",
                    "Import thành công: $insertedCount Todo mới"
                )

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Không thể tải Todo. " +
                                "Dữ liệu offline vẫn được giữ."
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(isLoading = false)
                }
            }
        }
    }
}
