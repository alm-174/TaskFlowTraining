package com.example.taskflowtraining.model


data class Task(
    val id: Long,
    val title: String,
    val isDone: Boolean = false,
    val priority: Int = 1,
    val remoteId: Int? = null
)

enum class TaskFilter { ALL, ACTIVE, COMPLETED }
enum class SortMode { NEWEST, TITLE }

fun validateTitle(raw: String): String {
    val title = raw.trim()
    require(title.isNotBlank()) { "Tên công việc không được trống" }
    require(title.length <= 100) { "Tên tối đa 100 ký tự" }
    return title
}

fun filterAndSort(tasks: List<Task>, query: String,
                  filter: TaskFilter, sort: SortMode, priority: Int? =null) : List<Task>
{
    val q = query.trim();
    val filtered  = tasks.filter { task ->
        task.title.contains(q, ignoreCase = true) &&
                (priority == null || task.priority == priority) &&
                when(filter){
                    TaskFilter.ALL -> true
                    TaskFilter.ACTIVE -> !task.isDone
                    TaskFilter.COMPLETED -> task.isDone
                }
    }


    return when(sort){
        SortMode.NEWEST -> filtered.sortedByDescending { it.id }
        SortMode.TITLE -> filtered.sortedWith (
            compareBy<Task> { it.title.lowercase() }. thenBy{it.id}
        )
    }
}