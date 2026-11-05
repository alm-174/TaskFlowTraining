package com.example.taskflowtraining

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.content.Intent
import android.widget.Button
import android.widget.LinearLayout
class DetailActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_RESULT = "extra_result"
    }

    override fun onStart() {
        super.onStart()
        Log.d("DetailLifecycle", "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("DetailLifecycle", "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d("DetailLifecycle", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("DetailLifecycle", "onStop")
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("DetailLifecycle", "onRestart")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("DetailLifecycle", "onDestroy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d("DetailLifecycle", "onCreate")

        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }

        val textView = TextView(this).apply {
            text = title
            textSize = 24f
        }

        val button = Button(this).apply {
            text = "Hoàn thành"
        }

        layout.addView(textView)
        layout.addView(button)

        setContentView(layout)

        button.setOnClickListener {
            val resultIntent = Intent().apply {
                putExtra(EXTRA_RESULT, "Đã hoàn thành: $title")
            }

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}