package com.example.taskflowtraining.composepractice

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TaskCounter(
    count: Int,
    onIncrement: () -> Unit
) {

    Log.d("ComposeTest", "CON: count = $count")

    Text(text = "$count")

    Button(onClick = onIncrement) {
        Text(text = "+")
    }
}

@Preview(showBackground = true)
@Composable
fun TaskCounterScreen() {
    var count by remember {
        mutableIntStateOf(0)
    }

    Log.d("ComposeTest", "CHA: count = $count")

    TaskCounter(
        count = count,
        onIncrement = {
            count++
        }
    )
}