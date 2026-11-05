package com.example.taskflowtraining

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskflowtraining.databinding.ActivityTaskBinding
import android.widget.Button
import android.widget.LinearLayout
class TaskActivity : AppCompatActivity() {
    private lateinit var b: ActivityTaskBinding
    private val tasks = mutableListOf<Task>()
    private var nextId = 1L


    private fun toggleTask(id: Long) {
        val i = tasks.indexOfFirst { it.id == id }

        if (i >= 0) {
            tasks[i] = tasks[i].copy(
                isDone = !tasks[i].isDone
            )
        }

        renderTasks()
    }

    private fun deleteTask(id: Long) {
        tasks.removeAll { it.id == id }
        renderTasks()
    }
    private fun renderTasks() {

        b.taskContainer.removeAllViews()

        for (task in tasks) {

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val textView = TextView(this).apply {
                text = "${task.id}. ${task.title}"
                textSize = 18f
                setPadding(8, 12, 8, 12)

                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            val btnToggle = Button(this).apply {
                text = if (task.isDone) "Undo" else "Done"

                setOnClickListener {
                    toggleTask(task.id)
                }
            }

            val btnDelete = Button(this).apply {
                text = "Xóa"

                setOnClickListener {
                    deleteTask(task.id)
                }
            }

            row.addView(textView)
            row.addView(btnToggle)
            row.addView(btnDelete)

            b.taskContainer.addView(row)
        }

        b.tvStats.text =
            "Tổng: ${tasks.size} | Đã xong: ${tasks.count { it.isDone }}"

        b.tvEmpty.visibility =
            if (tasks.isEmpty()) View.VISIBLE else View.GONE
    }
    private fun validateTitle(raw: String): String? {
        if (raw.isBlank()) {
            return "Tên công việc không được để trống"
        }

        return null
    }

    private fun addTask(raw: String) {
        if (validateTitle(raw) != null) return

        tasks += Task(nextId++, raw.trim())
        renderTasks()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(b.root)

        renderTasks()

        b.btnAdd.setOnClickListener {
            val title = b.etTitle.text.toString()

            val error = validateTitle(title)

            if (error != null) {
                b.etTitle.error = error
                return@setOnClickListener
            }

            addTask(title)
            b.etTitle.text.clear()
        }
    }
}