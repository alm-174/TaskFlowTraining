package com.example.taskflowtraining.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class TaskLogicTest {

    @Test
    fun trimsValidTitle() {
        val input = "  Học Kotlin  "

        val actual = validateTitle(input)

        assertEquals("Học Kotlin", actual)
    }

    @Test
    fun rejectsBlankTitle() {
        assertThrows(IllegalArgumentException::class.java) {
            validateTitle("   ")
        }
    }

    @Test
    fun rejectsEmptyTitle() {
        assertThrows(IllegalArgumentException::class.java) {
            validateTitle("")
        }
    }

    @Test
    fun acceptsExactly100Characters() {
        val input = "A".repeat(100)

        val actual = validateTitle(input)

        assertEquals(input, actual)
    }

    @Test
    fun rejectsMoreThan100Characters() {
        val input = "A".repeat(101)

        assertThrows(IllegalArgumentException::class.java) {
            validateTitle(input)
        }
    }

    @Test
    fun preservesVietnameseCharacters() {
        val input = "  Học lập trình Android  "

        val actual = validateTitle(input)

        assertEquals("Học lập trình Android", actual)
    }
}