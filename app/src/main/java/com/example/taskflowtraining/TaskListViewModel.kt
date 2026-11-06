package com.example.taskflowtraining

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class TaskListViewModel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Danh sách công việc
    val tasks = mutableListOf<Task>()

    // ID tiếp theo
    var nextId = 1L

    // Từ khóa tìm kiếm
    var searchQuery: String
        get() = savedStateHandle["searchQuery"] ?: ""
        set(value) {
            savedStateHandle["searchQuery"] = value
        }

    // Bộ lọc hiện tại
    var currentFilter: TaskFilter
        get() {
            val name: String =
                savedStateHandle["currentFilter"] ?: TaskFilter.ALL.name

            return TaskFilter.valueOf(name)
        }
        set(value) {
            savedStateHandle["currentFilter"] = value.name
        }
}