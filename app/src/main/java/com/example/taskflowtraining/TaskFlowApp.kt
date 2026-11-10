package com.example.taskflowtraining

import android.app.Application
import androidx.room.Room
import com.example.taskflowtraining.data.*
import com.example.taskflowtraining.data.local.TaskDatabase
import com.example.taskflowtraining.data.remote.TodoApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.taskflowtraining.settings.DataStoreSettingsRepository
class TaskFlowApp : Application() {
    val database by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "taskflow.db"
        ).build()
    }

    private val api by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
    }

    val taskRepository: TaskRepository by lazy {
        DefaultTaskRepository(database.taskDao(), api)
    }

    val settingsRepository by lazy {
        DataStoreSettingsRepository(applicationContext)
    }
}