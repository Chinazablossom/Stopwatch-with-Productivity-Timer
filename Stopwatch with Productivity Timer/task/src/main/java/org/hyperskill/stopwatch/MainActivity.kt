package org.hyperskill.stopwatch

import android.app.AlertDialog
import android.app.Notification
import android.app.Notification.FLAG_INSISTENT
import android.app.Notification.FLAG_ONLY_ALERT_ONCE
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import org.hyperskill.stopwatch.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var color: String
    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private var isRunning = false
    private var upperLimit = 0
    private val startTimer: Runnable = object : Runnable {
        override fun run() {
            elapsedTime = System.currentTimeMillis() - startTime
            updateTimerText(elapsedTime)
            if (isRunning) handler.postDelayed(this, 1000)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            startButton.setOnClickListener {
                if (!isRunning) startTimer()
            }

            resetButton.setOnClickListener {
                reset()
            }

            settingsButton.setOnClickListener {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val alertLayout = layoutInflater.inflate(R.layout.settings_dialog_layout, null)
                    val editTextValue = alertLayout.findViewById<EditText>(R.id.upperLimitEditText)
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Set Upper Limit")
                        .setMessage("Enter a number that'll be a limit for your timer")
                        .setView(alertLayout)
                        .setPositiveButton("OK") { _, _ ->
                            if (editTextValue.text.isNotBlank()) upperLimit =
                                editTextValue.text.toString().toInt()
                            else Toast.makeText(
                                this@MainActivity,
                                "You did not set an upper limit",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .setNegativeButton("Cancel") { _, _ ->
                            return@setNegativeButton
                        }
                        .create()
                        .show()
                }
            }


        }
    }

    private fun updateTimerText(elapsedMillis: Long) {
        val minutes = (elapsedMillis / 60000).toInt()
        val seconds = ((elapsedMillis % 60000) / 1000).toInt()
        binding.textView.apply {
            text = getString(R.string.time, minutes, seconds)
            if (upperLimit <= 0) return@apply
            if (seconds > upperLimit) {
                setTextColor(Color.RED)
                val intent = Intent(applicationContext, NotificationReceiver::class.java)
                sendBroadcast(intent)
            } else setTextColor(Color.BLACK)

        }

        color = generateRandomColor()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            binding.progressBar.indeterminateTintList =
                ColorStateList.valueOf(Color.parseColor(color))
        }
    }

    private fun startTimer() {
        isRunning = true
        startTime = System.currentTimeMillis()
        binding.apply {
            progressBar.visibility = VISIBLE
            settingsButton.isEnabled = false
        }
        handler.postDelayed(startTimer, 1000)
    }

    private fun stopTimer() {
        handler.removeCallbacks(startTimer)
    }

    private fun reset() {
        isRunning = false
        upperLimit = 0
        stopTimer()
        binding.apply {
            textView.apply {
                text = getString(R.string._00_00)
                setTextColor(Color.BLACK)
            }
            settingsButton.isEnabled = true
            progressBar.visibility = GONE
        }

    }

    private fun generateRandomColor(): String {
        val random = ("0123456789ABCDEF").toList().shuffled().joinToString("")
        val color = ("#FF${random.take(6)}")
        val isValidColor = "^#(((FF|ff)?([A-Fa-f0-9]{6}))|([A-Fa-f0-9]{3}))\$".toRegex()
        return if (color.matches(isValidColor)) color else ""
    }

    override fun onStop() {
        super.onStop()
        stopTimer()
    }

}

class NotificationReceiver : BroadcastReceiver() {
    private val channelId = "org.hyperskill"
    override fun onReceive(context: Context, intent: Intent) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val pIntent =
            PendingIntent.getActivity(context, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE)
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Upper limit channel"
            val descriptionText = "Your upper limit status"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, "org.hyperskill")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Your Upper Limit has been reached")
            .setContentText("The upper limit has been reached come back to the app and reset it")
            .setStyle(NotificationCompat.BigTextStyle())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pIntent)

        val notification = notificationBuilder.build()
        notification.flags = FLAG_ONLY_ALERT_ONCE or FLAG_INSISTENT



        notificationManager.notify(393939, notification)
    }
}