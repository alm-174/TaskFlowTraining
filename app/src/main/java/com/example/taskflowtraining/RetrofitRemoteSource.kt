
package com.example.taskflowtraining

class RetrofitRemoteSource(
    private val api: TodoApi
) : RemoteSource {

    override suspend fun getTodos(): List<TodoDto> {
        return api.getTodos()
    }

    override suspend fun getTodo(id: Int): TodoDto {
        return api.getTodo(id)
    }
}
