package com.example.taskflowtraining.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskflowtraining.data.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@HiltWorker
class RefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {

        return try {
            repository.importSample()
            Result.success()
        }catch (e : CancellationException){
            throw e
        } catch (e: IOException) {
            if (runAttemptCount < 2) {
                Result.retry()
            } else {
                Result.failure()
            }

        } catch (e: HttpException) {
            val retryable = e.code() == 429 || e.code() in 500..599

            if (retryable && runAttemptCount < 2) {
                Result.retry()
            } else {
                Result.failure()
            }
        }catch (e: Exception) {
            Result.failure()
        }

    }

}