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
    fun observeTaskById(id: Long): Flow<Task?>
    suspend fun add(title: String, priority : Int = 1)
    suspend fun setDone(id: Long, done: Boolean)
    suspend fun delete(id: Long)

    suspend fun updateTitle(id: Long, newTitle: String, newPriority: Int = 1)
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

    override fun observeTaskById(id: Long): Flow<Task?> {
        return dao.observeById(id).map { entity ->
            entity?.toModel()}
    }

    override suspend fun add(title: String, priority : Int) {
        require(priority in 1..3) {
            "Độ ưu tiên phải từ 1 đến 3"
        }
        dao.insert(TaskEntity(title = validateTitle(title), priority = priority))
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        val old = dao.findById(id) ?: return
        dao.update(old.copy(isDone = done))
    }

    override suspend fun delete(id: Long) {
        dao.findById(id)?.let { dao.delete(it) }
    }

    override suspend fun updateTitle(id: Long, newTitle: String, newPriority: Int) {
        val old = dao.findById(id)
            ?: throw IllegalArgumentException("Không tìm thấy công việc")
        require(newPriority in 1..3) {
            "Độ ưu tiên phải từ 1 đến 3"
        }
        dao.update(old.copy(title = validateTitle(newTitle), priority = newPriority))
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
