package com.example.taskflowtraining.settings

import com.example.taskflowtraining.model.SortMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeSettingsRepository : SettingsRepository {

    private val _sortMode = MutableStateFlow(SortMode.NEWEST)

    override val sortMode: StateFlow<SortMode> = _sortMode

    override suspend fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }
}