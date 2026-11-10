package com.example.taskflowtraining.model

import com.example.taskflowtraining.data.TaskRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException

class FakeTaskRepository : TaskRepository {

    private val tasks = MutableStateFlow<List<Task>>(emptyList())

    var failNextRefresh = false


    var operationDelayMillis = 0L


    override fun observeTasks(): Flow<List<Task>> {
        return tasks
    }


    override suspend fun add(title: String, priority: Int) {
        if (operationDelayMillis > 0) {
            delay(operationDelayMillis)
        }

        require(title.isNotBlank()) {
            "Tiêu đề không được để trống"
        }

        require(title.length <= 80) {
            "Tiêu đề tối đa 80 ký tự"
        }

        require(priority in 1..3) {
            "Priority phải từ 1 đến 3"
        }

        val newId = (tasks.value.maxOfOrNull { it.id } ?: 0L) + 1L

        val newTask = Task(
            id = newId,
            title = title,
            priority = priority
        )

        tasks.value = tasks.value + newTask
    }

    // 3. Đánh dấu hoàn thành hoặc chưa hoàn thành
    override suspend fun setDone(id: Long, done: Boolean) {
        tasks.value = tasks.value.map { task ->
            if (task.id == id) {
                task.copy(isDone = done)
            } else {
                task
            }
        }
    }

    // 4. Xóa một công việc
    override suspend fun delete(id: Long) {
        tasks.value = tasks.value.filterNot { it.id == id }
    }

    // 5. Sửa tiêu đề và Priority
    override suspend fun updateTitle(
        id: Long,
        title: String,
        priority: Int
    ) {
        require(title.isNotBlank()) {
            "Tiêu đề không được để trống"
        }

        require(title.length <= 80) {
            "Tiêu đề tối đa 80 ký tự"
        }

        require(priority in 1..3) {
            "Priority phải từ 1 đến 3"
        }

        tasks.value = tasks.value.map { task ->
            if (task.id == id) {
                task.copy(
                    title = title,
                    priority = priority
                )
            } else {
                task
            }
        }
    }

    // 6. Xóa tất cả công việc đã hoàn thành
    override suspend fun clearCompleted() {
        tasks.value = tasks.value.filterNot { it.isDone }
    }

    // 7. Import dữ liệu mẫu
    override suspend fun importSample(): Int {

        if (failNextRefresh) {
            failNextRefresh = false
            throw IOException("Giả lập lỗi mạng")
        }

        if (operationDelayMillis > 0) {
            kotlinx.coroutines.delay(operationDelayMillis)
        }

        val samples = listOf(
            Task(
                id = nextId(),
                title = "Học Kotlin",
                priority = 1
            ),
            Task(
                id = nextId() + 1,
                title = "Học Android",
                priority = 2
            ),
            Task(
                id = nextId() + 2,
                title = "Làm bài tập",
                priority = 3
            )
        )

        tasks.value = tasks.value + samples

        return samples.size
    }

    // 8. Hàm hỗ trợ: lấy ID tiếp theo
    private fun nextId(): Long {
        return (tasks.value.maxOfOrNull { it.id } ?: 0L) + 1L
    }

    // 9. Hàm hỗ trợ: thiết lập dữ liệu cho test
    fun setTasks(newTasks: List<Task>) {
        tasks.value = newTasks
    }

    // 10. Hàm hỗ trợ: đọc dữ liệu hiện tại
    fun currentTasks(): List<Task> {
        return tasks.value
    }

    // 11. Hàm hỗ trợ: xóa dữ liệu giả
    fun reset() {
        tasks.value = emptyList()
        failNextRefresh = false
        operationDelayMillis = 0L
    }
}
