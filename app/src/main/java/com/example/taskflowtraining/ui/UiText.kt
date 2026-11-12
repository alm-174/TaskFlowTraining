package com.example.taskflowtraining.ui

import android.content.Context
import com.example.taskflowtraining.R

data class UiText(
    val resourceId: Int,
    val args: List<Any> = emptyList()
) {
    fun resolve(context: Context): String =
        context.getString(resourceId, *args.toTypedArray())
}

// Domain validation messages remain independent of Android resources.
fun Throwable.toUiText(fallback: Int): UiText = UiText(
    when (message) {
        "Tên công việc không được trống" -> R.string.title_empty
        "Tên tối đa 100 ký tự" -> R.string.title_too_long
        "Độ ưu tiên phải từ 1 đến 3" -> R.string.priority_invalid
        "Không tìm thấy công việc" -> R.string.task_not_found
        else -> fallback
    }
)
