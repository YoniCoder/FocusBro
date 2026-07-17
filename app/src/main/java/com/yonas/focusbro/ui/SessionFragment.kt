package com.yonas.focusbro.ui

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.yonas.focusbro.MainActivity
import com.yonas.focusbro.R
import com.yonas.focusbro.data.AppDatabase
import com.yonas.focusbro.data.SessionEntity
import com.yonas.focusbro.service.TimerService
import com.yonas.focusbro.views.CircularTimerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SessionFragment : Fragment() {

    private lateinit var circularTimer: CircularTimerView
    private lateinit var timeDisplay: TextView
    private lateinit var timerType: TextView
    private lateinit var quoteText: TextView
    private lateinit var pauseButton: Button
    private lateinit var quitButton: Button
    private lateinit var tagText: TextView

    private var remainingSeconds = 25 * 60
    private var isRunning = false
    private var isPaused = false
    private var sessionTag = "Work"
    private var sessionDuration = 25
    private var handler = Handler(Looper.getMainLooper())

    private var backPressedTime: Long = 0
    private val BACK_PRESS_INTERVAL = 2000L

    private lateinit var backCallback: OnBackPressedCallback
    private var isCompleted = false

    private val quotes = listOf(
        "The finish line gets closer every second.",
        "Stay here. Stay focused.",
        "Focus is where dreams become plans.",
        "It does not matter how slowly you go as long as you do not stop.",
        "Don't watch the clock. Watch your progress.",
        "Distraction steals what consistency creates.",
        "One session today. A better tomorrow.",
        "Success loves consistency.",
        "Action beats intention every time.",
        "Consistency is a superpower.",
        "You don't need motivation. Just the next minute.",
        "Some days progress is simply not quitting.",
        "Difficult doesn't mean impossible.",
        "Keep moving, even if it's slowly.",
        "Every session counts.",
        "A rough day is still a day to grow.",
        "Don't chase perfection. Chase consistency.",
        "Your effort is never wasted.",
        "Protect your attention—it's your greatest asset.",
        "Focus on what moves the needle.",
        "Quality comes from uninterrupted effort.",
        "Your best work deserves your full attention.",
        "Keep going.",
        "Stay present.",
        "Focus wins.",
        "One task.",
        "Almost there.",
        "You've got this.",
        "Finish what you started.",
        "Keep growing.",
        "One more minute.",
        "Stay locked in.",
        "Don't watch the clock; do what it does. Keep going.",
        "Believe you can and you're halfway there.",
        "The best time to start was yesterday. The next best time is now.",
        "Your time is limited, don't waste it living someone else's life."
    )

    private val timerReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.getStringExtra("action")
            when (action) {
                "tick" -> {
                    val remaining = intent.getIntExtra(TimerService.EXTRA_REMAINING, 0)
                    remainingSeconds = remaining
                    updateDisplay()
                    val progress = remainingSeconds.toFloat() / (sessionDuration * 60)
                    circularTimer.setProgress(progress, true)
                }
                "complete" -> {
                    onTimerComplete()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_session, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).hideMenuIcon()

        circularTimer = view.findViewById(R.id.sessionCircularTimer)
        timeDisplay = view.findViewById(R.id.sessionTimeDisplay)
        timerType = view.findViewById(R.id.sessionTimerType)
        quoteText = view.findViewById(R.id.quoteText)
        pauseButton = view.findViewById(R.id.sessionPauseButton)
        quitButton = view.findViewById(R.id.quitButton)
        tagText = view.findViewById(R.id.sessionTag)

        sessionTag = arguments?.getString("tag") ?: "Work"
        tagText.text = sessionTag
        sessionDuration = arguments?.getInt("duration", 25) ?: 25
        val initialRemaining = arguments?.getInt("remaining_seconds", -1) ?: -1

        if (initialRemaining > 0) {
            remainingSeconds = initialRemaining
            isRunning = true
            isPaused = false
            pauseButton.text = "Pause"
        } else {
            remainingSeconds = sessionDuration * 60
            isRunning = false
            isPaused = false
            pauseButton.text = "Start"
            startTimer()
        }

        val randomQuote = quotes.random()
        quoteText.text = randomQuote
        quoteText.visibility = View.VISIBLE

        quitButton.visibility = View.VISIBLE
        quitButton.setOnClickListener { showQuitConfirmation() }

        pauseButton.setOnClickListener {
            if (isRunning && !isPaused) pauseTimer()
            else if (isPaused) resumeTimer()
            else startTimer()
        }

        updateDisplay()
        val progress = remainingSeconds.toFloat() / (sessionDuration * 60)
        circularTimer.setProgress(progress, false)
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(timerReceiver, IntentFilter(TimerService.BROADCAST_UPDATE))
    }

    override fun onPause() {
        super.onPause()
        try {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(timerReceiver)
        } catch (e: IllegalArgumentException) { /* already unregistered */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(updateRunnable)
        (activity as MainActivity).showMenuIcon()
        backCallback.remove()
    }

    private fun startTimer() {
        if (isRunning) return
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_START
            putExtra(TimerService.EXTRA_DURATION, sessionDuration * 60)
            putExtra(TimerService.EXTRA_TAG, sessionTag)
        }
        requireContext().startService(intent)
        isRunning = true
        isPaused = false
        pauseButton.text = "Pause"
        circularTimer.setColor(resources.getColor(android.R.color.holo_green_dark))
    }

    private fun pauseTimer() {
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_PAUSE
        }
        requireContext().startService(intent)
        isPaused = true
        pauseButton.text = "Resume"
        handler.removeCallbacks(updateRunnable)
    }

    private fun resumeTimer() {
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_RESUME
        }
        requireContext().startService(intent)
        isPaused = false
        pauseButton.text = "Pause"
    }

    private fun stopTimerService() {
        val intent = Intent(requireContext(), TimerService::class.java).apply {
            action = TimerService.ACTION_STOP
        }
        requireContext().startService(intent)
        isRunning = false
        isPaused = false
    }

    private fun onTimerComplete() {
        if (isCompleted) return
        isCompleted = true

        stopTimerService()
        pauseButton.text = "Start"
        circularTimer.setColor(resources.getColor(android.R.color.holo_red_dark))
        timerType.text = "✅ Completed!"

        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        val currentCount = prefs.getInt("total_sessions", 0)
        prefs.edit().putInt("total_sessions", currentCount + 1).apply()

        // Sound
        val soundEnabled = prefs.getBoolean("sound_enabled", true)
        if (soundEnabled) {
            try {
                val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                if (notificationUri != null) {
                    val mediaPlayer = MediaPlayer.create(requireContext(), notificationUri)
                    mediaPlayer?.setOnCompletionListener { it.release() }
                    mediaPlayer?.start()
                }
            } catch (e: Exception) { e.printStackTrace() }
        }

        // Vibration
        try {
            val vibrator = requireContext().getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(500)
                }
            }
        } catch (e: Exception) { e.printStackTrace() }

        // Save session
        CoroutineScope(Dispatchers.IO).launch {
            val session = SessionEntity(
                tag = sessionTag,
                durationMinutes = sessionDuration,
                timestamp = System.currentTimeMillis()
            )
            AppDatabase.getInstance(requireContext()).sessionDao().insertSession(session)
        }

        // *** SAFE DIALOG WITH DELAY ***
        showCompletionDialog()
    }

    private fun showCompletionDialog() {
        // Post to main thread to allow UI to settle
        Handler(Looper.getMainLooper()).postDelayed({
            // Double-check fragment is still attached
            if (!isAdded || isDetached || activity == null || context == null) {
                (activity as? MainActivity)?.clearBackStackAndNavigate(HomeFragment())
                return@postDelayed
            }
            try {
                AlertDialog.Builder(requireContext())
                    .setTitle("🎉 Congratulations!")
                    .setMessage("You did it! Great focus session.")
                    .setPositiveButton("Refocus") { _, _ ->
                        if (isAdded && !isDetached) {
                            (activity as? MainActivity)?.clearBackStackAndNavigate(HomeFragment())
                        }
                    }
                    .setNegativeButton("See Progress") { _, _ ->
                        if (isAdded && !isDetached) {
                            (activity as? MainActivity)?.goToProgressWithHomeBack()
                        }
                    }
                    .setCancelable(false)
                    .show()
            } catch (e: Exception) {
                Log.e("SessionFragment", "Completion dialog error", e)
                (activity as? MainActivity)?.clearBackStackAndNavigate(HomeFragment())
            }
        }, 100) // small delay to ensure UI is ready
    }

    private fun showQuitConfirmation() {
        if (!isAdded || isDetached || context == null) {
            (activity as? MainActivity)?.goHome()
            return
        }
        try {
            AlertDialog.Builder(requireContext())
                .setTitle("Quit Session?")
                .setMessage("Are you sure you want to quit? Your progress will be lost.")
                .setPositiveButton("Yes, Quit") { _, _ ->
                    if (isAdded && !isDetached) {
                        stopTimerService()
                        (activity as? MainActivity)?.goHome()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            Log.e("SessionFragment", "Quit dialog error", e)
            if (isAdded && !isDetached) {
                stopTimerService()
                (activity as? MainActivity)?.goHome()
            }
        }
    }

    private fun updateDisplay() {
        val minutes = remainingSeconds / 60
        val seconds = remainingSeconds % 60
        timeDisplay.text = String.format("%02d:%02d", minutes, seconds)
    }

    fun handleBackPressed() {
        if (backPressedTime + BACK_PRESS_INTERVAL > System.currentTimeMillis()) {
            showQuitConfirmation()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(requireContext(), "Press back again to quit", Toast.LENGTH_SHORT).show()
        }
    }

    private val updateRunnable = object : Runnable {
        override fun run() { /* Not used */ }
    }

    companion object {
        fun newInstance(tag: String, duration: Int = 25, remainingSeconds: Int = -1): SessionFragment {
            return SessionFragment().apply {
                arguments = Bundle().apply {
                    putString("tag", tag)
                    putInt("duration", duration)
                    putInt("remaining_seconds", remainingSeconds)
                }
            }
        }
    }
}