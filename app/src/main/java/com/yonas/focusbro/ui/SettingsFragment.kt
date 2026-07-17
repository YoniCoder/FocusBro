package com.yonas.focusbro.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.switchmaterial.SwitchMaterial
import com.yonas.focusbro.R

class SettingsFragment : Fragment() {

    private lateinit var soundSwitch: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        soundSwitch = view.findViewById(R.id.soundSwitch)

        loadSettings()

        soundSwitch.setOnCheckedChangeListener { _, _ -> saveSettings() }
    }

    private fun loadSettings() {
        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
    }

    private fun saveSettings() {
        val prefs = requireContext().getSharedPreferences("FocusBroSettings", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putBoolean("sound_enabled", soundSwitch.isChecked)
            apply()
        }
    }
}