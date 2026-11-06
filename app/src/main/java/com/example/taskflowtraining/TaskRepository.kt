
package com.example.taskflowtraining

import kotlinx.coroutines.flow.Flow

interface TaskRepository {

    fun observeTasks(): Flow<List<Task>>

    suspend fun add(
        title: String,
        priority: TaskPriority = TaskPriority.MEDIUM
    )

    suspend fun toggle(id: Long)

    suspend fun delete(id: Long)

    suspend fun refreshRemote(): Int
}
