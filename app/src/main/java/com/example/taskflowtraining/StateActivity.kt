package com.example.taskflowtraining

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.taskflowtraining.databinding.ActivityStateBinding

class StateActivity : AppCompatActivity() {

    private lateinit var b: ActivityStateBinding

    private var count = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        b = ActivityStateBinding.inflate(layoutInflater)
        setContentView(b.root)

        count = savedInstanceState?.getInt("count") ?: 0

        val name = savedInstanceState?.getString("name").orEmpty()
        b.etName.setText(name)

        render()

        b.btnIncrease.setOnClickListener {
            count++
            render()
        }
    }

    private fun render() {
        b.tvCount.text = count.toString()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("count", count)
        outState.putString("name", b.etName.text.toString())

        super.onSaveInstanceState(outState)
    }
}