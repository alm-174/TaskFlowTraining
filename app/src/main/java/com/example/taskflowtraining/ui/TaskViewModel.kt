package com.example.taskflowtraining.ui

import androidx.lifecycle.*
import com.example.taskflowtraining.R
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
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.taskflowtraining.data.worker.RefreshWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.TimeUnit

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val query: String = "",
    val filter: TaskFilter = TaskFilter.ALL,
    val sort: SortMode = SortMode.NEWEST,
    val priorityFilter: Int? = null,
    val isLoading: Boolean = false,
    val error: UiText? = null,
    val message: UiText? = null
)


@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repo: TaskRepository,
    private val settings: SettingsRepository,
    private val workManager: WorkManager
) : ViewModel() {
    private val query = MutableStateFlow("")
    private val filter = MutableStateFlow(TaskFilter.ALL)

    private val priorityFilter = MutableStateFlow<Int?>(null)
    private val busy = MutableStateFlow(false)
    private val error = MutableStateFlow<UiText?>(null)
    private val message = MutableStateFlow<UiText?>(null)
    private val mutex = Mutex()

    private val importWorkName = "taskflow_sample_import"
    private val selectedImportId = MutableStateFlow<UUID?>(null)

    private val acknowledgedImportId = MutableStateFlow<UUID?>(null)

    val importWorkInfo: StateFlow<WorkInfo?> =
        combine(
            workManager.getWorkInfosForUniqueWorkFlow(importWorkName),
            selectedImportId
        ) { workInfos, selectedId ->
            when {
                selectedId != null ->
                    workInfos.firstOrNull { it.id == selectedId }

                else ->
                    workInfos.firstOrNull { !it.state.isFinished }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    val importResultToShow: StateFlow<WorkInfo?> =
        combine(
            importWorkInfo,
            acknowledgedImportId
        ) { workInfo, acknowledgedId ->

            if (
                workInfo != null &&
                workInfo.state.isFinished &&
                workInfo.id != acknowledgedId
            ) {
                workInfo
            } else {
                null
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )
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
        success: UiText? = null,
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
                    error.value = UiText(R.string.error_http, listOf(e.code()))
                } catch (e: IOException) {
                    error.value = UiText(R.string.error_network)
                } catch (e: Exception) {
                    error.value = e.toUiText(R.string.error_generic)
                } finally {
                    busy.value = false
                }
            }
        }
    }

    fun add(raw: String, priority : Int = 1) = runAction(UiText(R.string.task_added)) {
        repo.add(raw, priority)
    }

    fun setDone(task: Task, done: Boolean) = runAction {
        repo.setDone(task.id, done)
    }

    fun delete(task: Task) = runAction(UiText(R.string.task_deleted)) {
        repo.delete(task.id)
    }

    fun updateTask(task: Task, newTitle: String, newPriority: Int = 1) = runAction(UiText(R.string.task_updated)){
        repo.updateTitle(task.id, newTitle, newPriority)
    }

    fun clearCompleted() = runAction(UiText(R.string.completed_cleared)) {
        repo.clearCompleted()
    }

    fun importSample() {
        viewModelScope.launch {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = OneTimeWorkRequestBuilder<RefreshWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .build()

            val operation = workManager.enqueueUniqueWork(
                importWorkName,
                ExistingWorkPolicy.KEEP,
                request
            )

            try {
                withContext(Dispatchers.IO) {
                    operation.result.get()
                }

                val workInfos = withContext(Dispatchers.IO) {
                    workManager
                        .getWorkInfosForUniqueWork(importWorkName)
                        .get()
                }

                val activeWork = workInfos.firstOrNull {
                    !it.state.isFinished
                }

                selectedImportId.value =
                    activeWork?.id
                        ?: workInfos.firstOrNull {
                            it.id == request.id
                        }?.id

            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = e.toUiText(R.string.error_enqueue)
            }
        }
    }

    fun setSort(value: SortMode) = runAction {
        settings.setSortMode(value)
    }

    fun dismissFeedback() {
        error.value = null
        message.value = null
    }

    fun acknowledgeImportResult(workId: UUID) {
        acknowledgedImportId.value = workId
    }

}