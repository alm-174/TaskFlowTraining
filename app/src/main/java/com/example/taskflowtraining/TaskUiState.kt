package com.example.taskflowtraining

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val currentFilter: TaskFilter = TaskFilter.ALL,
    val isLoading: Boolean = false,
    val error: String? = null
)