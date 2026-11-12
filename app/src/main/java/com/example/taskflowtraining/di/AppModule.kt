package com.example.taskflowtraining.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.example.taskflowtraining.data.DefaultTaskRepository
import com.example.taskflowtraining.data.TaskRepository
import com.example.taskflowtraining.data.local.MIGRATION_1_2
import com.example.taskflowtraining.data.local.TaskDatabase
import com.example.taskflowtraining.data.remote.TodoApi
import com.example.taskflowtraining.settings.DataStoreSettingsRepository
import com.example.taskflowtraining.settings.SettingsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): TaskDatabase {
        return Room.databaseBuilder(
            context,
            TaskDatabase::class.java,
            "taskflow.db"
        )
            .addMigrations(MIGRATION_1_2).build()
    }

    @Provides
    fun provideTaskDao(
        database: TaskDatabase
    ) = database.taskDao()

    @Provides
    @Singleton
    fun provideTodoApi(): TodoApi {
        return Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TodoApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTaskRepository(
        database: TaskDatabase,
        api: TodoApi
    ): TaskRepository {
        return DefaultTaskRepository(database.taskDao(), api)
    }

    @Provides
    @Singleton
    fun provideSettingsRepository(
        @ApplicationContext context: Context
    ): SettingsRepository {
        return DataStoreSettingsRepository(context)
    }

    @Provides
    @Singleton
    fun provideWorkManager(
        @ApplicationContext context: Context
    ): WorkManager {
        return WorkManager.getInstance(context)
    }
}