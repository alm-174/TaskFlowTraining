package com.example.taskflowtraining

import android.app.Application
import androidx.room.Room
import com.example.taskflowtraining.data.*
import com.example.taskflowtraining.data.local.TaskDatabase
import com.example.taskflowtraining.data.remote.TodoApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.example.taskflowtraining.settings.DataStoreSettingsRepository
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TaskFlowApp : Application()