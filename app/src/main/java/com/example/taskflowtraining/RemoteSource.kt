
package com.example.taskflowtraining

interface RemoteSource {

    suspend fun getTodos(): List<TodoDto>

    suspend fun getTodo(id: Int): TodoDto
}
