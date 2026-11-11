package com.example.taskflowtraining.ui

import androidx.lifecycle.*
import com.example.taskflowtraining.data.TaskRepository
import com.example.taskflowtraining.model.*
import com.example.taskflowtraining.settings.SettingsRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException
import retrofit2.HttpException
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: SortMode = SortMode.NEWEST,
    val priorityFilter: Int? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val settings: SettingsRepository
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)

    private val priorityFilter = MutableStateFlow<Int?>(null)
    private val busy = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val message = MutableStateFlow<String?>(null)
    private val mutex = Mutex()

    private data class ListState(
        val tasks: List<Task>,
        val query: String,
        val filter: TaskFilter,
        val sort: SortMode,
        val priorityFilter: Int?
    )

    private val listState = combine(
        repo.observeTasks(), query, filter, settings.sortMode, priorityFilter
    ) { tasks, q, f, sort, p ->
        ListState(filterAndSort(tasks, q, f, sort, p), q, f, sort, p)
    }

    val uiState: StateFlow<TaskUiState> = combine(
        listState, busy, error, message
    ) { list, loading, err, msg ->
        TaskUiState(
            tasks = list.tasks, query = list.query,
            filter = list.filter, sort = list.sort, priorityFilter = list.priorityFilter,
            isLoading = loading, error = err, message = msg
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TaskUiState()
    )

    fun setQuery(value: String) { query.value = value }
    fun setFilter(value: TaskFilter) { filter.value = value }

    fun setPriorityFilter(value: Int?) {
        priorityFilter.value = value
    }
    private fun runAction(
        success: String? = null,
        action: suspend () -> Unit
    ) {
        viewModelScope.launch {
            mutex.withLock {
                busy.value = true
                error.value = null
                message.value = null
                try {
                    action()
                    if (success != null) message.value = success
                } catch (e: CancellationException) {
                    throw e
                } catch (e: HttpException) {
                    error.value = "HTTP ${e.code()}: không tải được dữ liệu"
                } catch (e: IOException) {
                    error.value = "Lỗi mạng hoặc lưu trữ: ${e.message}"
                } catch (e: Exception) {
                    error.value = e.message ?: "Có lỗi xảy ra"
                } finally {
                    busy.value = false
                }
            }
        }
    }

    fun add(raw: String, priority : Int = 1) = runAction("Đã thêm công việc") {
        repo.add(raw, priority)
    }

    fun setDone(task: Task, done: Boolean) = runAction {
        repo.setDone(task.id, done)
    }

    fun delete(task: Task) = runAction("Đã xóa công việc") {
        repo.delete(task.id)
    }

    fun updateTask(task: Task, newTitle: String, newPriority: Int = 1) = runAction("Đã sửa tiêu đề"){
        repo.updateTitle(task.id, newTitle, newPriority)
    }

    fun clearCompleted() = runAction("Đã xóa công việc hoàn thành") {
        repo.clearCompleted()
    }

    fun importSample() = runAction {
        val count = repo.importSample()
        message.value = "Đã thêm $count công việc mẫu"
    }

    fun setSort(value: SortMode) = runAction {
        settings.setSortMode(value)
    }

    fun dismissFeedback() {
        error.value = null
        message.value = null
    }



}