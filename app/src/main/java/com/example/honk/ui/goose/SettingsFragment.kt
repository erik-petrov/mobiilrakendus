package com.example.honk.ui.goose

import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.navigation.fragment.findNavController
import com.example.honk.R
import com.example.honk.notifications.DailySummaryPrefs
import com.example.honk.notifications.DailySummaryScheduler
import java.util.Locale


class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val themeSwitch = view.findViewById<SwitchCompat>(R.id.themeSwitch)

        // Setup current state
        val currentNightMode = requireContext().getSharedPreferences("app_theme", Context.MODE_PRIVATE)
            .getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        themeSwitch.isChecked = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            val nightMode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            // Save setting
            requireContext().getSharedPreferences("app_theme", Context.MODE_PRIVATE)
                .edit()
                .putInt("night_mode", nightMode)
                .apply()

            // Apply theme
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }

        // --- Daily summary controls ---
        val dailySwitch = view.findViewById<SwitchCompat>(R.id.dailySummarySwitch)
        val dailyTime = view.findViewById<TextView>(R.id.dailySummaryTime)

        fun renderTime() {
            val h = DailySummaryPrefs.getHour(requireContext())
            val m = DailySummaryPrefs.getMinute(requireContext())
            dailyTime.text = String.format(Locale.getDefault(), "%02d:%02d", h, m)
        }

        dailySwitch.isChecked = DailySummaryPrefs.isEnabled(requireContext())
        renderTime()

        dailySwitch.setOnCheckedChangeListener { _, enabled ->
            DailySummaryPrefs.setEnabled(requireContext(), enabled)
            if (enabled) DailySummaryScheduler.scheduleNext(requireContext())
            else DailySummaryScheduler.cancel(requireContext())
        }

        dailyTime.setOnClickListener {
            val h = DailySummaryPrefs.getHour(requireContext())
            val m = DailySummaryPrefs.getMinute(requireContext())

            TimePickerDialog(requireContext(), { _, hourOfDay, minute ->
                DailySummaryPrefs.setTime(requireContext(), hourOfDay, minute)
                renderTime()
                if (DailySummaryPrefs.isEnabled(requireContext())) {
                    DailySummaryScheduler.scheduleNext(requireContext())
                }
            }, h, m, true).show()
        }
    }
}