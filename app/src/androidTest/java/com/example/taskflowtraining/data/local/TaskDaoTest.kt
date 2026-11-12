package com.example.taskflowtraining.data.local

import android.icu.text.CaseMap
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskflowtraining.model.Task
import com.example.taskflowtraining.model.validateTitle
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows

@RunWith(AndroidJUnit4::class)
class TaskDaoTest {

    private lateinit var database: TaskDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TaskDatabase::class.java
        ).build()

        dao = database.taskDao()
    }



    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertTask_shouldSaveTask() = runTest {
        val task = TaskEntity(
            title = "Học Room Testing"
        )

        val id = dao.insert(task)

        val saveTask = dao.findById(id)

        assertNotNull(saveTask)
        assertEquals("Học Room Testing", saveTask?.title)

    }

    @Test
    fun observeAll_shouldReturnInsertedTasks() = runTest {
        val task = TaskEntity(
            title = "Học Kotlin Flow"
        )

        dao.insert(task)

        val tasks = dao.observeAll().first()

        assertEquals(1, tasks.size)
        assertEquals("Học Kotlin Flow", tasks.first().title)
    }

    @Test
    fun updateTask_shouldChangeIsDone() = runTest {
        val task = TaskEntity(
            title = "Update "
        )

        val id = dao.insert(task)

        val savedTask = dao.findById(id)?: throw IllegalArgumentException("Không tìm thấy công việc")

        dao.update(savedTask.copy(title = "Update Task"))

        val newTask = dao.findById(id)?: throw IllegalArgumentException("Không tìm thấy công việc")

        assertEquals("Update Task", newTask?.title)
    }

    @Test
    fun deleteTask_shouldRemoveTask() = runTest {
        val task = TaskEntity(
            title = "Delete Task"
        )

        val id = dao.insert(task)

        val savedTask = dao.findById(id)
            ?: error("Không tìm thấy công việc")

        dao.delete(savedTask)

        val deletedTask = dao.findById(id)

        assertNull(deletedTask)
    }
}