package com.example.taskflowtraining.data.local

import TaskEntity
import com.example.taskflowtraining.Task
import com.example.taskflowtraining.TaskPriority

fun TaskEntity.toTask(): Task {
    return Task(
        id = id,
        title = title,
        isDone = isDone,
        priority = when (priority) {
            0 -> TaskPriority.LOW
            2 -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        isDone = isDone,
        priority = when (priority) {
            TaskPriority.LOW -> 0
            TaskPriority.MEDIUM -> 1
            TaskPriority.HIGH -> 2
        }
    )
}