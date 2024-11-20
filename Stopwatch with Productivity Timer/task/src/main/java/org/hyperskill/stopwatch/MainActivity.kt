package org.hyperskill.stopwatch

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import org.hyperskill.stopwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding :ActivityMainBinding
    private var startedTime:Long = 0
    private var elapsedTime:Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isStarted = false
    private val incrementTimer =  object: Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startedTime
            updateTimerText(elapsedTime)
            handler.postDelayed(this,1000)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            startButton.setOnClickListener {
                if (!isStarted) startTimer()
            }

            resetButton.setOnClickListener {
                reset()
            }
        }

    }

    private fun updateTimerText(elapsedMillis: Long) {
        val minutes = (elapsedMillis / 60000).toInt()
        val seconds = ((elapsedMillis % 60000) / 1000).toInt()
        binding.textView.text = getString(R.string.time, minutes, seconds)
    }

    private fun startTimer() {
        startedTime = System.currentTimeMillis()
        handler.postDelayed(incrementTimer, 1000)
        isStarted = true
    }

    private fun reset() {
        isStarted = false
        binding.textView.text = getString(R.string._00_00)
        handler.removeCallbacks(incrementTimer)
    }

    override fun onStop() {
        super.onStop()
        reset()
    }

}