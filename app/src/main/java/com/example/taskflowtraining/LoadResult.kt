
package com.example.taskflowtraining

sealed interface LoadResult<out T> {

    data class Success<T>(
        val data: T
    ) : LoadResult<T>

    data class Failure(
        val message: String
    ) : LoadResult<Nothing>
}
