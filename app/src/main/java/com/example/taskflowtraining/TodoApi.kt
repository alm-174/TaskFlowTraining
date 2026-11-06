package com.example.taskflowtraining

import retrofit2.http.GET
import retrofit2.http.Path

interface TodoApi {

    @GET("todos")
    suspend fun getTodos(): List<TodoDto>

    @GET("todos/{id}")
    suspend fun getTodo(
        @Path("id") id: Int
    ): TodoDto
}