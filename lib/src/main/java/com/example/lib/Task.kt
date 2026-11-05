package com.example.lib

data class Task(
    val id: Long, val title: String,
    val isDone: Boolean = false, val priority: Int = 1
)
fun pending(xs: List<Task>) = xs.filterNot { it.isDone }
    .sortedWith(
        compareByDescending<Task> { it.priority }
            .thenBy { it.id }
    )
interface TaskStore {
    fun all(): List<Task>
    fun save(task: Task)

    fun delete(id : Long)

    fun toggle(id : Long)
}
class InMemoryTaskStore : TaskStore {
    private val items = mutableListOf<Task>()
    override fun all() = items.toList()
    override fun save(task: Task) {
        items.removeAll { it.id == task.id }
        items.add(task)
    }

    override fun delete(id: Long) {
        items.removeAll { it.id == id }
    }

    override fun toggle(id: Long) {
        val task = items.find{it.id == id}

        if(task != null)
        {
            val updateTask = task.copy(isDone = !task.isDone)
            save(updateTask)
        }
    }
}