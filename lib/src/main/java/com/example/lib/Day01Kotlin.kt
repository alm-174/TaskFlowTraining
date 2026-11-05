package com.example.lib

fun validateTitle(raw: String?): String? {
    val title = raw?.trim().orEmpty()
    return when {
        title.isBlank() -> "Tiêu đề không được trống"
        title.length > 80 -> "Tối đa 80 ký tự"
        else -> null
    }
}

fun grade(score: Int): String? {
    return when{
        score !in 0..100 -> "INVALID"
        score >= 85 -> "A"
        score >= 70 -> "B"
        score >= 50 -> "C"
        else -> "D"
    }
}

fun sumPositiveEven(numbers: List<Int>): Int{
    return numbers.filter { it > 0 && it % 2 == 0 }.sum();
}

fun validatePassword(password: String): List<String> {
    return buildList {
        if (password.length < 8) {
            add("Mật khẩu phải có ít nhất 8 ký tự")
        }

        if (password.none { it.isLetter() }) {
            add("Mật khẩu phải có ít nhất một chữ cái")
        }

        if (password.none { it.isDigit() }) {
            add("Mật khẩu phải có ít nhất một chữ số")
        }
    }
}

fun main() {
    listOf(-1, 0, 49, 50, 69, 70, 84, 85, 100, 101)
        .forEach { println(grade(it)) }

    val numbers = listOf(2, -4, 3, 6)
    println(sumPositiveEven(numbers))

    val passwords = listOf(
        "abc12345",
        "abc",
        "12345678",
        "abcdefgh",
        ""
    )

    passwords.forEach { password ->
        val errors = validatePassword(password)

        println("Password: '$password'")

        if (errors.isEmpty()) {
            println("Hop le")
        } else {
            errors.forEach { error ->
                println(error)
            }
        }
    }
}