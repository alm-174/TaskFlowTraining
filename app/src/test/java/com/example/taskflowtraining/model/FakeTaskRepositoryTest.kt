
package com.example.taskflowtraining.model

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class FakeTaskRepositoryTest {

    @Test
    fun importSample_success() = runTest {
        // Arrange
        val fake = FakeTaskRepository()

        // Act
        val count = fake.importSample()

        // Assert
        assertEquals(3, count)
        assertEquals(3, fake.currentTasks().size)
    }

    @Test
    fun importSample_ioException_keepsOldTasks() = runTest {
        // Arrange
        val fake = FakeTaskRepository()

        fake.add("Công việc cũ", 1)
        fake.failNextRefresh = true

        // Act
        var caughtException: IOException? = null

        try {
            fake.importSample()
        } catch (e: IOException) {
            caughtException = e
        }

        // Assert
        assertNotNull(caughtException)
        assertEquals(1, fake.currentTasks().size)
        assertEquals(
            "Công việc cũ",
            fake.currentTasks()[0].title
        )
    }

    @Test
    fun delete_nonExistingId_keepsOldTasks() = runTest {
        // Arrange
        val fake = FakeTaskRepository()
        fake.add("Học Kotlin", 1)
        fake.add("Học Android", 2)

        // Act
        fake.delete(99L)

        // Assert
        assertEquals(2, fake.currentTasks().size)
        assertEquals("Học Kotlin", fake.currentTasks()[0].title)
        assertEquals("Học Android", fake.currentTasks()[1].title)
    }
}
