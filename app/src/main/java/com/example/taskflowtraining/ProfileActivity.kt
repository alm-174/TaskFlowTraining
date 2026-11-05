package com.example.taskflowtraining

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflowtraining.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_NAME = "extra_name"
        const val EXTRA_AGE = "extra_age"
        const val EXTRA_RESULT = "extra_result"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val b = ActivityProfileBinding.inflate(layoutInflater)

        setContentView(b.root)

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val age = intent.getIntExtra(EXTRA_AGE, 0)

        b.tvProfile.text = "Tên: $name\nTuổi: $age"

        b.btnReturn.setOnClickListener {

            val resultIntent = Intent().apply {
                putExtra(EXTRA_RESULT, "Đã nhận hồ sơ: $name, $age tuổi")
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}