package com.example.taskflowtraining.settings

import com.example.taskflowtraining.model.SortMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {

    val sortMode: Flow<SortMode>

    suspend fun setSortMode(mode: SortMode)
}