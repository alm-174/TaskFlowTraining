package com.example.taskflowtraining

data class Task(
    val id: Long,
    val title: String,
    val isDone: Boolean = false,
    val priority: TaskPriority = TaskPriority.MEDIUM
)