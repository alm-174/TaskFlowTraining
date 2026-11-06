
package com.example.taskflowtraining

fun TodoDto.toTask(): Task {
    return Task(
        id = id.toLong(),
        title = title,
        isDone = completed,
        priority = TaskPriority.MEDIUM
    )
}
