package com.yonas.focusbro.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.yonas.focusbro.MainActivity
import com.yonas.focusbro.R
import com.yonas.focusbro.data.AppDatabase
import com.yonas.focusbro.data.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var startButton: Button
    private lateinit var editTimeButton: ImageButton
    private lateinit var tagSpinner: Spinner
    private lateinit var timeDisplay: TextView
    private lateinit var sessionCounter: TextView

    private var selectedTag = ""
    private var sessionCount = 0

    private var workDuration = 25
    private var shortBreak = 5
    private var longBreak = 15

    private lateinit var tagAdapter: ArrayAdapter<String>
    private val tagList = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startButton = view.findViewById(R.id.startButton)
        editTimeButton = view.findViewById(R.id.editTimeButton)
        tagSpinner = view.findViewById(R.id.tagSpinner)
        timeDisplay = view.findViewById(R.id.timeDisplay)
        sessionCounter = view.findViewById(R.id.sessionCounter)

        loadSettings()

        tagAdapter = ArrayAdapter(requireContext(), R.layout.spinner_item, tagList).apply {
            setDropDownViewResource(R.layout.spinner_dropdown_item)
        }
        tagSpinner.adapter = tagAdapter

        loadTagsFromDatabase()

        tagSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position >= 0 && position < tagList.size) {
                    selectedTag = tagList[position]
                    updateTimeDisplay()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        editTimeButton.setOnClickListener {
            if (selectedTag.isNotEmpty()) {
                showEditTimeDialog()
            } else {
                Toast.makeText(requireContext(), "No tag selected", Toast.LENGTH_SHORT).show()
            }
        }


        startButton.setOnClickListener {
            if (selectedTag.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a tag", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val duration = getDurationForTag(selectedTag)
            val sessionFragment = SessionFragment.newInstance(selectedTag, duration)
            (activity as MainActivity).replaceFragment(sessionFragment)
        }

        updateSessionCounter()
    }

    override fun onResume() {
        super.onResume()
        loadTagsFromDatabase()
        updateSessionCounter()
    }

    private fun updateSessionCounter() {
        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        sessionCount = prefs.getInt("total_sessions", 0)
        sessionCounter.text = "$sessionCount sessions today"
    }

    private fun loadTagsFromDatabase() {
        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(requireContext()).tagDao()
            var tags = dao.getAllTags()

            // Remove duplicates by name - use distinctBy
            val uniqueTags = tags.distinctBy { it.name }
            if (uniqueTags.size != tags.size) {
                // Delete all and reinsert unique ones
                tags.forEach { dao.deleteTag(it) }
                uniqueTags.forEach { dao.insertTag(it) }
                tags = dao.getAllTags()
                // Re-run distinct to be safe
                val finalTags = tags.distinctBy { it.name }
                if (finalTags.size != tags.size) {
                    // If still duplicates, force clean by deleting all and reinserting unique ones
                    tags.forEach { dao.deleteTag(it) }
                    finalTags.forEach { dao.insertTag(it) }
                    tags = dao.getAllTags()
                }
            }

            if (tags.isEmpty()) {
                val defaults = listOf("Work", "Study", "Reading", "Exercise", "Code")
                defaults.forEach { name ->
                    dao.insertTag(Tag(name = name))
                }
                tags = dao.getAllTags()
            }

            val tagNames = tags.map { it.name }.distinct()
            withContext(Dispatchers.Main) {
                updateTagList(tagNames)
            }
        }
    }

    private fun updateTagList(tagNames: List<String>) {
        tagList.clear()
        tagList.addAll(tagNames)
        tagAdapter.notifyDataSetChanged()

        if (tagList.isNotEmpty()) {
            selectedTag = tagList[0]
            tagSpinner.setSelection(0)
            updateTimeDisplay()
        } else {
            selectedTag = ""
            timeDisplay.text = "00:00"
        }
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        workDuration = prefs.getInt("work_duration", 25)
        shortBreak = prefs.getInt("short_break", 5)
        longBreak = prefs.getInt("long_break", 15)
    }

    private fun getDurationForTag(tag: String): Int {
        return when (tag) {
            "Work" -> workDuration
            "Study" -> shortBreak
            "Reading" -> longBreak
            "Exercise" -> workDuration
            "Code" -> shortBreak
            else -> workDuration
        }
    }

    private fun updateTimeDisplay() {
        if (selectedTag.isEmpty()) return
        val duration = getDurationForTag(selectedTag)
        timeDisplay.text = String.format("%02d:%02d", duration, 0)
    }

    private fun showEditTimeDialog() {
        val currentDuration = getDurationForTag(selectedTag)
        val input = EditText(requireContext())
        input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
        input.setText(currentDuration.toString())
        input.hint = "Enter minutes (1-180)"

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Time for \"$selectedTag\"")
            .setMessage("Enter duration in minutes (1-180)")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val value = input.text.toString().toIntOrNull()
                if (value != null && value in 1..180) {
                    saveDurationForTag(selectedTag, value)
                    updateTimeDisplay()
                } else {
                    Toast.makeText(requireContext(), "Invalid duration", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun saveDurationForTag(tag: String, duration: Int) {
        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        when (tag) {
            "Work" -> {
                workDuration = duration
                prefs.edit().putInt("work_duration", duration).apply()
            }
            "Study" -> {
                shortBreak = duration
                prefs.edit().putInt("short_break", duration).apply()
            }
            "Reading" -> {
                longBreak = duration
                prefs.edit().putInt("long_break", duration).apply()
            }
            "Exercise" -> {
                workDuration = duration
                prefs.edit().putInt("work_duration", duration).apply()
            }
            "Code" -> {
                shortBreak = duration
                prefs.edit().putInt("short_break", duration).apply()
            }
            else -> {
                workDuration = duration
                prefs.edit().putInt("work_duration", duration).apply()
            }
        }
    }

    fun updateSessionCount(count: Int) {
        sessionCount = count
        sessionCounter.text = "$sessionCount sessions today"
    }
}