package org.hyperskill.stopwatch

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import org.hyperskill.stopwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private var startedTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())
    private var isStarted = false
    private var color: String = ""
    private val incrementTimer = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startedTime
            updateTimerText(elapsedTime)
            handler.postDelayed(this, 1000)
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
        color = generateRandomColor()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.progressBar.indeterminateTintList =
                ColorStateList.valueOf(Color.parseColor(color))
        }
    }

    private fun startTimer() {
        startedTime = System.currentTimeMillis()
        binding.progressBar.visibility = VISIBLE
        handler.postDelayed(incrementTimer, 1000)
        isStarted = true
    }

    private fun reset() {
        isStarted = false
        binding.apply {
            textView.text = getString(R.string._00_00)
            progressBar.visibility = GONE
        }
        handler.removeCallbacks(incrementTimer)
    }

    private fun generateRandomColor(): String {
        val random = ("0123456789ABCDEF").toList().shuffled().joinToString("")
        val color = ("#FF${random.take(6)}")
        val isValidColor = "^#(((FF|ff)?([A-Fa-f0-9]{6}))|([A-Fa-f0-9]{3}))\$".toRegex()
        return if (color.matches(isValidColor)) color else "aaa"
    }

    override fun onStop() {
        super.onStop()
        reset()
    }

}