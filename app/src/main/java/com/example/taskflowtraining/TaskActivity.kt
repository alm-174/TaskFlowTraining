package com.example.taskflowtraining

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflowtraining.databinding.ActivityTaskBinding

class TaskActivity : AppCompatActivity() {

    private lateinit var b: ActivityTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityTaskBinding.inflate(layoutInflater)
        setContentView(b.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragmentContainer,
                    TaskListFragment()
                )
                .commit()
        }
    }
}