package com.example.taskflowtraining.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFilterAndSortTest {

    private val tasks = listOf(
        Task(id = 1, title = "Học Kotlin", isDone = false),
        Task(id = 2, title = "Học Android", isDone = true),
        Task(id = 3, title = "Làm bài tập", isDone = false),
        Task(id = 4, title = "Đọc sách", isDone = true)
    )

    @Test
    fun allFilterReturnsAllTasks() {
        val result = filterAndSort(
            tasks,
            query = "",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertEquals(4, result.size)
        assertEquals(listOf(4L, 3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun activeFilterReturnsOnlyIncompleteTasks() {
        val result = filterAndSort(
            tasks,
            query = "",
            filter = TaskFilter.ACTIVE,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf(3L, 1L), result.map { it.id })
        assertTrue(result.all { !it.isDone })
    }

    @Test
    fun completedFilterReturnsOnlyCompletedTasks() {
        val result = filterAndSort(
            tasks,
            query = "",
            filter = TaskFilter.COMPLETED,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf(4L, 2L), result.map { it.id })
        assertTrue(result.all { it.isDone })
    }

    @Test
    fun searchIgnoresCase() {
        val result = filterAndSort(
            tasks,
            query = "KOTLIN",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf("Học Kotlin"), result.map { it.title })
    }

    @Test
    fun searchTrimsWhitespace() {
        val result = filterAndSort(
            tasks,
            query = "  Android  ",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf("Học Android"), result.map { it.title })
    }

    @Test
    fun sortNewestUsesDescendingId() {
        val result = filterAndSort(
            tasks,
            query = "",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf(4L, 3L, 2L, 1L), result.map { it.id })
    }

    @Test
    fun sortTitleUsesAlphabeticalOrder() {
        val input = listOf(
            Task(id = 1, title = "Zebra"),
            Task(id = 2, title = "apple"),
            Task(id = 3, title = "Banana")
        )

        val result = filterAndSort(
            input,
            query = "",
            filter = TaskFilter.ALL,
            sort = SortMode.TITLE
        )

        assertEquals(
            listOf("apple", "Banana", "Zebra"),
            result.map { it.title }
        )
    }

    @Test
    fun sameTitleSortsByIdAscending() {
        val input = listOf(
            Task(id = 5, title = "Kotlin"),
            Task(id = 2, title = "kotlin"),
            Task(id = 8, title = "KOTLIN")
        )

        val result = filterAndSort(
            input,
            query = "",
            filter = TaskFilter.ALL,
            sort = SortMode.TITLE
        )

        assertEquals(listOf(2L, 5L, 8L), result.map { it.id })
    }

    @Test
    fun filterAndSearchWorkTogether() {
        val result = filterAndSort(
            tasks,
            query = "Học",
            filter = TaskFilter.ACTIVE,
            sort = SortMode.NEWEST
        )

        assertEquals(listOf("Học Kotlin"), result.map { it.title })
    }

    @Test
    fun noMatchingTasksReturnsEmptyList() {
        val result = filterAndSort(
            tasks,
            query = "Python",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun emptyInputReturnsEmptyList() {
        val result = filterAndSort(
            emptyList(),
            query = "",
            filter = TaskFilter.ALL,
            sort = SortMode.NEWEST
        )

        assertTrue(result.isEmpty())
    }
}