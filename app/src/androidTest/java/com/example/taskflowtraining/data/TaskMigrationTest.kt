package com.example.taskflowtraining.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.taskflowtraining.data.local.MIGRATION_1_2
import com.example.taskflowtraining.data.local.TaskDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        TaskDatabase::class.java
    )

    @Test
    fun migrate1To2_preservesData() {
        val dbName = "migration-test"

        val db = helper.createDatabase(dbName, 1)

        db.execSQL(
            """
        INSERT INTO tasks (id, title, isDone, priority, remoteId)
        VALUES (1, 'Học Room Migration', 0, 2, NULL)
        """.trimIndent()
        )

        db.close()

        val migratedDb = helper.runMigrationsAndValidate(
            dbName,
            2,
            true,
            MIGRATION_1_2
        )

        try {
            migratedDb.query(
                "SELECT id, title, isDone, priority, description FROM tasks WHERE id = 1"
            ).use { cursor ->

                assertTrue(cursor.moveToFirst())

                assertEquals(
                    "Học Room Migration",
                    cursor.getString(cursor.getColumnIndexOrThrow("title"))
                )

                assertEquals(
                    2,
                    cursor.getInt(cursor.getColumnIndexOrThrow("priority"))
                )

                assertEquals(
                    0,
                    cursor.getInt(cursor.getColumnIndexOrThrow("isDone"))
                )

                assertEquals(
                    "",
                    cursor.getString(cursor.getColumnIndexOrThrow("description"))
                )
            }
        } finally {
            migratedDb.close()
        }

    }
}