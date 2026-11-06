
package com.example.taskflowtraining

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTaskRepository : TaskRepository {

    private val _tasks =
        MutableStateFlow<List<Task>>(emptyList())

    private var nextId = 1L

    override fun observeTasks(): Flow<List<Task>> {
        return _tasks.asStateFlow()
    }

    // Thêm công việc kèm Priority
    override suspend fun add(
        title: String,
        priority: TaskPriority
    ) {
        val task = Task(
            id = nextId++,
            title = title,
            isDone = false,
            priority = priority
        )

        _tasks.update { currentTasks ->
            currentTasks + task
        }
    }

    // Toggle theo ID
    override suspend fun toggle(id: Long) {
        _tasks.update { currentTasks ->
            currentTasks.map { task ->
                if (task.id == id) {
                    task.copy(isDone = !task.isDone)
                } else {
                    task
                }
            }
        }
    }

    // Xóa theo ID
    override suspend fun delete(id: Long) {
        _tasks.update { currentTasks ->
            currentTasks.filterNot { task ->
                task.id == id
            }
        }
    }

    override suspend fun refreshRemote(): Int {
        return 0
    }
}
