
package com.example.taskflowtraining

import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

class RemoteTaskRepository(
    private val remoteSource: RemoteSource
) {

    suspend fun getTodos(): LoadResult<List<Task>> {
        return try {
            val dtos = retryIo {
                remoteSource.getTodos()
            }

            val tasks = dtos.map { dto ->
                dto.toTask()
            }

            LoadResult.Success(tasks)

        } catch (e: CancellationException) {
            throw e

        } catch (e: IOException) {
            LoadResult.Failure("Không thể kết nối tới server")

        } catch (e: HttpException) {
            LoadResult.Failure("Lỗi HTTP ${e.code()}")

        } catch (e: Exception) {
            LoadResult.Failure("Không thể xử lý dữ liệu từ server")
        }
    }

    suspend fun getTodo(id: Int): LoadResult<Task> {
        return try {
            val dto = retryIo {
                remoteSource.getTodo(id)
            }

            LoadResult.Success(dto.toTask())

        } catch (e: CancellationException) {
            throw e

        } catch (e: IOException) {
            LoadResult.Failure("Không thể kết nối tới server")

        } catch (e: HttpException) {
            LoadResult.Failure(
                if (e.code() == 404)
                    "Không tìm thấy Todo có ID $id"
                else
                    "Lỗi HTTP ${e.code()}"
            )

        } catch (e: Exception) {
            LoadResult.Failure("Không thể xử lý dữ liệu từ server")
        }
    }
}
