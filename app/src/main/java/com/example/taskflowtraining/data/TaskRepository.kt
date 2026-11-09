package com.example.taskflowtraining.data

import com.example.taskflowtraining.data.local.TaskDao
import com.example.taskflowtraining.data.local.TaskEntity
import com.example.taskflowtraining.data.remote.TodoApi
import com.example.taskflowtraining.model.Task
import com.example.taskflowtraining.model.validateTitle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    suspend fun add(title: String)
    suspend fun setDone(id: Long, done: Boolean)
    suspend fun delete(id: Long)
    suspend fun clearCompleted()
    suspend fun importSample(): Int
}

class DefaultTaskRepository(
    private val dao: TaskDao,
    private val api: TodoApi
) : TaskRepository{
    override fun observeTasks(): Flow<List<Task>> {
        return dao.observeAll().map { rows -> rows.map{it.toModel()} }
    }

    override suspend fun add(title: String) {
        dao.insert(TaskEntity(title = validateTitle(title)))
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        val old = dao.findById(id) ?: return
        dao.update(old.copy(isDone = done))
    }

    override suspend fun delete(id: Long) {
        dao.findById(id)?.let { dao.delete(it) }
    }

    override suspend fun clearCompleted() {
        dao.deleteCompleted()
    }

    override suspend fun importSample(): Int {
        val remote = api.getTodos().take(20)
        var inserted = 0
        for (dto in remote) {
            val entity = dto.toEntity() ?: continue
            if (dao.insert(entity) != -1L) inserted++
        }
        return inserted
    }

}
