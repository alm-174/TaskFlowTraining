package com.example.taskflowtraining

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflowtraining.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var b: ActivityMainBinding

    private val profileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == RESULT_OK) {

            val message = result.data
                ?.getStringExtra(ProfileActivity.EXTRA_RESULT)
                .orEmpty()

            b.tvResult.text = message
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Khởi tạo ViewBinding trước
        b = ActivityMainBinding.inflate(layoutInflater)
        setContentView(b.root)

        b.btnOpenTasks.setOnClickListener {
            startActivity(Intent(this, TaskActivity::class.java))
        }


        b.btnOpenState.setOnClickListener {
            startActivity(Intent(this, StateActivity::class.java))
        }


        b.btnOpenProfile.setOnClickListener {

            val name = b.etName.text.toString().trim()
            val age = b.etAge.text.toString().toIntOrNull()

            if (name.isBlank()) {
                b.etName.error = getString(R.string.name_required)
                return@setOnClickListener
            }

            if (age == null || age <= 0) {
                b.etAge.error = getString(R.string.invalid_age)
                return@setOnClickListener
            }

            val intent = Intent(this, ProfileActivity::class.java).apply {
                putExtra(ProfileActivity.EXTRA_NAME, name)
                putExtra(ProfileActivity.EXTRA_AGE, age)
            }

            profileLauncher.launch(intent)
        }
    }
}