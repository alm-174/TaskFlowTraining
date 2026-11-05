package com.example.lib

fun calculateStats(tasks: List<Task>): TaskStats {
    val total = tasks.size
    val done = tasks.count { it.isDone }
    val pending = tasks.count { !it.isDone }

    return TaskStats(total, done, pending)
}

fun searchTasks(tasks: List<Task>, q: String): List<Task> {
    return tasks
        .filter {
            it.title.contains(q.trim(), ignoreCase = true)
        }
        .sortedWith(
            compareByDescending<Task> { it.priority }
                .thenBy { it.title }
        )
}

fun main() {

    val store: TaskStore = InMemoryTaskStore()

    store.save(Task(1L, "Học Kotlin", false))
    store.save(Task(2L, "Học Android", true))
    store.save(Task(3L, "Làm bài tập", false))

    val stats = calculateStats(store.all())

    println(stats)

    val result = searchTasks(store.all(), " kotlin ")

    result.forEach {
        println("${it.title} - Priority: ${it.priority}")
    }
}
