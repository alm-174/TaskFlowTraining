package com.example.taskflowtraining

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(
    name = "settings"
)

class SettingsRepository(
    private val context: Context
) {
    private val SORT_MODE = stringPreferencesKey("sort_mode")

    val sortMode: Flow<SortMode> =
        context.settingsStore.data.map { preferences ->
            val savedValue = preferences[SORT_MODE]

            SortMode.entries.firstOrNull {
                it.name == savedValue
            } ?: SortMode.DEFAULT
        }

    suspend fun setSortMode(mode: SortMode) {
        context.settingsStore.edit { preferences ->
            preferences[SORT_MODE] = mode.name
        }
    }
}