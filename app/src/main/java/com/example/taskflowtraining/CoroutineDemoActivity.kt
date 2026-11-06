package com.example.taskflowtraining

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.taskflowtraining.databinding.ActivityCoroutineDemoBinding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class CoroutineDemoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCoroutineDemoBinding

    private var job: Job? = null

    private suspend fun apiA(): String {
        delay(5000)
        return "API A thành công"
    }

    private suspend fun apiB(): String {
        delay(2000)
        throw IllegalStateException("API B gặp lỗi!")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoroutineDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLoad.setOnClickListener {
            testNonBlocking()
        }

        binding.btnCancel.setOnClickListener {
            job?.cancel()
        }
    }

    private fun startLoading() {
        job?.cancel()

        job = lifecycleScope.launch {

            binding.progressBar.isVisible = true
            binding.tvResult.text = "Đang tải..."

            try {
                Log.d("CoroutineDemo", "Bắt đầu tải")

                delay(3000)

                binding.tvResult.text = "Hoàn thành"
                Log.d("CoroutineDemo", "Tải thành công")

            } catch (e: CancellationException) {

                binding.tvResult.text = "Đã hủy"
                Log.d("CoroutineDemo", "Coroutine bị hủy")

                throw e

            } finally {

                binding.progressBar.isVisible = false
                Log.d("CoroutineDemo", "Finally được gọi")
            }
        }
    }

    private fun startCountdown() {
        job?.cancel()

        job = lifecycleScope.launch {
            binding.progressBar.isVisible = true

            try {
                for (i in 5 downTo 1) {
                    binding.tvResult.text = "Còn $i giây"
                    delay(1000)
                }

                binding.tvResult.text = "Hoàn thành!"

            } catch (e: CancellationException) {
                binding.tvResult.text = "Đã hủy!"
                throw e

            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    private fun testParallelError() {
        job?.cancel()

        job = lifecycleScope.launch {
            binding.progressBar.isVisible = true
            binding.tvResult.text = "Đang gọi API..."

            try {
                val result = coroutineScope {
                    val a = async { apiA() }
                    val b = async { apiB() }

                    "${a.await()} - ${b.await()}"
                }

                binding.tvResult.text = result

            } catch (e: CancellationException) {
                binding.tvResult.text = "Đã hủy"
                throw e

            } catch (e: Exception) {
                binding.tvResult.text = "Lỗi: ${e.message}"

            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }


    private fun testSupervisorScope() {
        job?.cancel()

        job = lifecycleScope.launch {
            binding.progressBar.isVisible = true
            binding.tvResult.text = "Đang gọi 2 API..."

            try {
                val results = supervisorScope {

                    val a = async {
                        try {
                            apiA()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            "API A lỗi: ${e.message}"
                        }
                    }

                    val b = async {
                        try {
                            apiB()
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            "API B lỗi: ${e.message}"
                        }
                    }

                    awaitAll(a, b)
                }

                binding.tvResult.text = results.joinToString("\n")

            } catch (e: CancellationException) {
                binding.tvResult.text = "Đã hủy"
                throw e

            } finally {
                binding.progressBar.isVisible = false
            }
        }
    }

    private fun testBlocking() {
        binding.tvResult.text = "Đang xử lý..."
        binding.progressBar.isVisible = true

        Thread.sleep(5000)

        binding.tvResult.text = "Hoàn thành"
        binding.progressBar.isVisible = false
    }

    private fun testNonBlocking() {
        job = lifecycleScope.launch {
            binding.tvResult.text = "Đang xử lý..."
            binding.progressBar.isVisible = true

            delay(5000)

            binding.tvResult.text = "Hoàn thành"
            binding.progressBar.isVisible = false
        }
    }

    override fun onDestroy() {
        Log.d("CoroutineDemo", "Activity onDestroy")
        super.onDestroy()
    }
}