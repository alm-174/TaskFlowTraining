package com.example.taskflowtraining

import android.app.Application
import androidx.room.Room
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.taskflowtraining.data.local.RoomTaskRepository
import com.example.taskflowtraining.data.local.TaskDatabase
import com.example.taskflowtraining.data.local.MIGRATION_1_2

class TaskFlowApp : Application() {

    val database: TaskDatabase by lazy {
        Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "taskflow_database"
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    val todoApi: TodoApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
    }

    val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(
            taskDao = database.taskDao(),
            todoApi = todoApi
        )
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(applicationContext)
    }
}