package com.example.taskflowtraining.settings

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.taskflowtraining.model.SortMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.taskflowDataStore by preferencesDataStore(
    name = "taskflow_settings"
)


class SettingsRepository(context: Context) {
    private val store = context.applicationContext.taskflowDataStore
    private val sortKey = stringPreferencesKey("sort_mode")
    val sortMode: Flow<SortMode> = store.data.map { preferences ->
        val raw = preferences[sortKey]
        SortMode.entries.firstOrNull { it.name == raw }
            ?: SortMode.NEWEST
    }

    suspend fun setSortMode(mode: SortMode) {
        store.edit { it[sortKey] = mode.name }
    }
}