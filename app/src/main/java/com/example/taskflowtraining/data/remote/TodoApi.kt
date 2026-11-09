package com.example.taskflowtraining.data.remote

import retrofit2.http.GET


data class TodoDto(
    val userId: Int,
    val id: Int,
    val title: String,
    val completed: Boolean
)

interface TodoApi {
    @GET("todos")
    suspend fun getTodos(): List<TodoDto>
}