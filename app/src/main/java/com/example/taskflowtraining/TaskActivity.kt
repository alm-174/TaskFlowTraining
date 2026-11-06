package com.example.taskflowtraining

import TaskEntity
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflowtraining.databinding.ActivityTaskBinding
import android.content.Intent
import android.util.Log
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
class TaskActivity : AppCompatActivity() {

    private lateinit var b: ActivityTaskBinding

    private fun testRoomDatabase() {
        lifecycleScope.launch {

            val dao = (application as TaskFlowApp)
                .database
                .taskDao()

            // 1. INSERT - Thêm Task
            val newId = dao.insert(
                TaskEntity(
                    title = "Học Room Database",
                    isDone = false,
                    priority = 2
                )
            )

            Log.d("ROOM_TEST", "INSERT: id = $newId")

            // 2. READ - Đọc Task vừa thêm
            val task = dao.findById(newId)

            Log.d("ROOM_TEST", "READ: $task")

            // 3. UPDATE - Đánh dấu hoàn thành
            if (task != null) {
                dao.update(
                    task.copy(isDone = true)
                )
            }

            val updatedTask = dao.findById(newId)

            Log.d("ROOM_TEST", "UPDATE: $updatedTask")

            // 4. DELETE - Xóa Task
            // Tạm thời chưa xóa để kiểm tra Database Inspector
            // dao.deleteById(newId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(b.root)

        // Nút mở màn hình Coroutine Demo
        b.btnCoroutineDemo.setOnClickListener {
            startActivity(
                Intent(this, CoroutineDemoActivity::class.java)
            )
        }

        // Hiển thị TaskListFragment như cũ
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, TaskListFragment())
                .commit()
        }

    }
}