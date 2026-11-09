package com.example.taskflowtraining.data

import com.example.taskflowtraining.data.local.TaskEntity
import com.example.taskflowtraining.data.remote.TodoDto
import com.example.taskflowtraining.model.Task

fun TaskEntity.toModel() = Task(
    id = id, title = title, isDone = isDone,
    priority = priority, remoteId = remoteId
)

fun TodoDto.toEntity(): TaskEntity? {
    val clean = title.trim().take(100)
    if (id <= 0 || clean.isBlank()) return null
    return TaskEntity(
        title = clean,
        isDone = completed,
        remoteId = id
    )
}