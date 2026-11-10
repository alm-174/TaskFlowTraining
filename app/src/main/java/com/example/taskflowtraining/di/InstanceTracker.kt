package com.example.taskflowtraining.di

import android.util.Log
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InstanceTracker @Inject constructor() {

    val id: String = UUID.randomUUID().toString()

    init {
        Log.d("HILT_SCOPE", "Created: $id")
    }
}