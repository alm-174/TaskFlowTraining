package com.example.taskflowtraining

import com.example.taskflowtraining.di.InstanceTracker
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@HiltAndroidTest
class HiltInjectionTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var tracker1: InstanceTracker

    @Inject
    lateinit var tracker2: InstanceTracker

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun singleton_shouldReturnSameInstance() {
        assertSame(tracker1, tracker2)
    }
}