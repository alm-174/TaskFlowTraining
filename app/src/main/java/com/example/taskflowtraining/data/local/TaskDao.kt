package com.example.taskflowtraining.data.local

import TaskEntity
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: TaskEntity): Long

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): TaskEntity?

    // Đếm tổng số Task
    @Query("SELECT COUNT(*) FROM tasks")
    fun observeTaskCount(): Flow<Int>

    // Đếm Task đã hoàn thành
    @Query("SELECT COUNT(*) FROM tasks WHERE isDone = 1")
    fun observeCompletedCount(): Flow<Int>

    // Lọc Task theo Priority
    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY id DESC")
    fun observeByPriority(priority: Int): Flow<List<TaskEntity>>
}