
package com.example.taskflowtraining

import java.io.IOException
import retrofit2.HttpException
import retrofit2.Response

class FakeRemoteSource : RemoteSource {

    enum class Mode {
        SUCCESS,
        NETWORK_ERROR,
        HTTP_500
    }

    var mode = Mode.SUCCESS

    var failNext = false

    private val fakeTodos = listOf(
        TodoDto(1, 1, "Học Kotlin", false),
        TodoDto(1, 2, "Học Retrofit", true),
        TodoDto(1, 3, "Học Repository", false)
    )

    private fun checkError() {
        if (failNext) {
            failNext = false
            throw IOException("Giả lập lỗi mạng một lần")
        }

        when (mode) {
            Mode.SUCCESS -> Unit

            Mode.NETWORK_ERROR -> {
                throw IOException("Giả lập mất kết nối")
            }

            Mode.HTTP_500 -> {
                throw HttpException(
                    Response.error<Any>(
                        500,
                        okhttp3.ResponseBody.create(
                            null,
                            "Internal Server Error"
                        )
                    )
                )
            }
        }
    }

    override suspend fun getTodos(): List<TodoDto> {
        checkError()
        return fakeTodos
    }

    override suspend fun getTodo(id: Int): TodoDto {
        checkError()

        return fakeTodos.find { it.id == id }
            ?: throw HttpException(
                Response.error<Any>(
                    404,
                    okhttp3.ResponseBody.create(
                        null,
                        "Not Found"
                    )
                )
            )
    }
}
