package com.example.taskflowtraining.settings

import com.example.taskflowtraining.model.SortMode
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeSettingsRepositoryTest {

    @Test
    fun setSortMode_updatesValue() = runTest {

        // Arrange
        val fake = FakeSettingsRepository()

        // Act
        fake.setSortMode(SortMode.TITLE)

        // Assert
        assertEquals(
            SortMode.TITLE,
            fake.sortMode.value
        )
    }
}