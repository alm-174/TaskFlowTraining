
package com.example.taskflowtraining

import kotlinx.coroutines.delay
import java.io.IOException

suspend fun <T> retryIo(
    block: suspend () -> T
): T {
    var attempt = 0

    while (true) {
        try {
            return block()
        } catch (e: IOException) {
            if (attempt++ >= 2) {
                throw e
            }

            delay(500L * attempt)
        }
    }
}
