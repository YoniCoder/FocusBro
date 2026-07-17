package com.yonas.focusbro.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.yonas.focusbro.MainActivity
import com.yonas.focusbro.R

class TimerService : Service() {

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_RESUME = "ACTION_RESUME"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION = "EXTRA_DURATION"
        const val EXTRA_TAG = "EXTRA_TAG"
        const val EXTRA_REMAINING = "EXTRA_REMAINING"
        const val BROADCAST_UPDATE = "com.yonas.focusbro.TIMER_UPDATE"
    }

    private var remainingSeconds = 0
    private var totalSeconds = 0
    private var isRunning = false
    private var isPaused = false
    private var tag = ""
    private val handler = Handler(Looper.getMainLooper())
    private var updateRunnable: Runnable? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var wakeLockHeld = false

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "FocusBro::TimerWakeLock"
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getIntExtra(EXTRA_DURATION, 25 * 60)
                tag = intent.getStringExtra(EXTRA_TAG) ?: "Work"
                totalSeconds = duration
                remainingSeconds = duration
                isRunning = true
                isPaused = false
                startForegroundService()
                startCountdown()
                acquireWakeLock()
            }
            ACTION_PAUSE -> {
                pauseTimer()
            }
            ACTION_RESUME -> {
                resumeTimer()
            }
            ACTION_STOP -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundService() {
        createNotificationChannel()
        val notification = buildNotification()
        startForeground(1, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "timer_channel",
                "FocusBro Timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows your timer progress"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        val timeText = String.format("%02d:%02d", minutes, seconds)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_session", true)
            putExtra("tag", tag)
            putExtra("duration", totalSeconds / 60)
            putExtra("remaining", remainingSeconds)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("FocusBro")
            .setContentText("$tag - $timeText remaining - Bro get back to your work!")
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun startCountdown() {
        updateRunnable = object : Runnable {
            override fun run() {
                if (isRunning && !isPaused) {
                    remainingSeconds--
                    updateNotification()
                    sendBroadcastUpdate()

                    if (remainingSeconds <= 0) {
                        onTimerComplete()
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(updateRunnable!!)
    }

    private fun pauseTimer() {
        isPaused = true
        releaseWakeLock()
    }

    private fun resumeTimer() {
        isPaused = false
        acquireWakeLock()
        updateRunnable?.let {
            handler.removeCallbacks(it)
            handler.post(it)
        } ?: run {
            startCountdown()
        }
    }

    private fun stopTimer() {
        isRunning = false
        updateRunnable?.let { handler.removeCallbacks(it) }
        releaseWakeLock()
        stopForeground(true)
        stopSelf()
    }

    private fun onTimerComplete() {
        isRunning = false
        updateRunnable?.let { handler.removeCallbacks(it) }
        releaseWakeLock()
        stopForeground(true)

        val intent = Intent(BROADCAST_UPDATE).apply {
            putExtra("action", "complete")
            putExtra(EXTRA_TAG, tag)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        stopSelf()
    }

    private fun updateNotification() {
        val notification = buildNotification()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1, notification)
    }

    private fun sendBroadcastUpdate() {
        val intent = Intent(BROADCAST_UPDATE).apply {
            putExtra("action", "tick")
            putExtra(EXTRA_REMAINING, remainingSeconds)
            putExtra(EXTRA_TAG, tag)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    // -------------------------------------------
    // WakeLock helpers with safety checks
    // -------------------------------------------
    private fun acquireWakeLock() {
        if (!wakeLockHeld && wakeLock != null) {
            try {
                wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
                wakeLockHeld = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun releaseWakeLock() {
        if (wakeLockHeld && wakeLock != null) {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock?.release()
                }
                wakeLockHeld = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        updateRunnable?.let { handler.removeCallbacks(it) }
        releaseWakeLock()
    }
}