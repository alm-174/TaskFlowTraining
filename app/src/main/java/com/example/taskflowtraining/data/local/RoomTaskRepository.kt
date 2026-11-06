package com.example.taskflowtraining.data.local

import TaskEntity
import com.example.taskflowtraining.Task
import com.example.taskflowtraining.TaskPriority
import com.example.taskflowtraining.TaskRepository
import com.example.taskflowtraining.TodoApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTaskRepository(
    private val taskDao: TaskDao,
    private val todoApi: TodoApi
) : TaskRepository {

    override fun observeTasks(): Flow<List<Task>> {
        return taskDao.observeAll().map { entities ->
            entities.map { entity ->
                entity.toTask()
            }
        }
    }

    override suspend fun add(
        title: String,
        priority: TaskPriority
    ) {
        val entity = TaskEntity(
            title = title,
            isDone = false,
            priority = when (priority) {
                TaskPriority.LOW -> 0
                TaskPriority.MEDIUM -> 1
                TaskPriority.HIGH -> 2
            }
        )

        taskDao.insert(entity)
    }

    override suspend fun toggle(id: Long) {
        val entity = taskDao.findById(id) ?: return

        val updatedEntity = entity.copy(
            isDone = !entity.isDone
        )

        taskDao.update(updatedEntity)
    }

    override suspend fun delete(id: Long) {
        taskDao.deleteById(id)
    }

    override suspend fun refreshRemote(): Int {
        val remoteTodos = todoApi.getTodos().take(20)

        var insertedCount = 0

        remoteTodos.forEach { todo ->
            val result = taskDao.insert(
                TaskEntity(
                    remoteId = todo.id,
                    title = todo.title,
                    isDone = todo.completed,
                    priority = 1
                )
            )

            if (result != -1L) {
                insertedCount++
            }
        }

        return insertedCount
    }
}