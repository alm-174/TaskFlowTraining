package com.example.taskflowtraining

import com.example.taskflowtraining.model.FakeTaskRepository
import com.example.taskflowtraining.settings.FakeSettingsRepository
import com.example.taskflowtraining.ui.TaskViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

import kotlinx.coroutines.flow.collect


@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun addTask_callsRepository() = runTest(
        mainDispatcherRule.dispatcher.scheduler
    ) {
        // Arrange
        val fakeRepo = FakeTaskRepository()
        val fakeSettings = FakeSettingsRepository()

        val vm = TaskViewModel(fakeRepo, fakeSettings)

        // Act
        vm.add("Học Android", 3)

        advanceUntilIdle()

        // Assert
        assertEquals(1, fakeRepo.currentTasks().size)
        assertEquals("Học Android", fakeRepo.currentTasks()[0].title)
        assertEquals(3, fakeRepo.currentTasks()[0].priority)
    }


    @Test
    fun loading_isTrueWhileAddingTask() = runTest(
        mainDispatcherRule.dispatcher.scheduler
    ) {
        // Arrange
        val fakeRepo = FakeTaskRepository()
        val fakeSettings = FakeSettingsRepository()

        fakeRepo.operationDelayMillis = 5_000

        val vm = TaskViewModel(fakeRepo, fakeSettings)

        // Bắt đầu collect uiState
        val job = backgroundScope.launch(
            kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        ) {
            vm.uiState.collect()
        }

        // Act: Yêu cầu thêm công việc
        vm.add("Học Android", 3)

        // Cho coroutine bắt đầu chạy
        runCurrent()

        // Assert: Đang thêm công việc
        assertTrue(vm.uiState.value.isLoading)

        // Tiến thời gian ảo thêm 5 giây
        advanceTimeBy(5_000)
        runCurrent()

        // Assert: Đã thêm xong
        assertFalse(vm.uiState.value.isLoading)

        assertEquals(1, fakeRepo.currentTasks().size)

        job.cancel()
    }

    @Test
    fun ioException_updatesErrorState() = runTest(
        mainDispatcherRule.dispatcher.scheduler
    ) {
        // Arrange
        val fakeRepo = FakeTaskRepository()
        val fakeSettings = FakeSettingsRepository()

        fakeRepo.failNextRefresh = true

        val vm = TaskViewModel(fakeRepo, fakeSettings)

        val job = backgroundScope.launch(
            kotlinx.coroutines.test.UnconfinedTestDispatcher(testScheduler)
        ) {
            vm.uiState.collect()
        }

        // Act
        vm.importSample()

        advanceUntilIdle()

        // Assert
        assertFalse(vm.uiState.value.isLoading)

        assertEquals(
            R.string.error_network,
            vm.uiState.value.error?.resourceId
        )

        assertEquals(0, fakeRepo.currentTasks().size)

        job.cancel()
    }

}