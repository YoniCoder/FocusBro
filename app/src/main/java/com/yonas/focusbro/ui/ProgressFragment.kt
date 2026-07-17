package com.yonas.focusbro.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.tabs.TabLayout
import com.yonas.focusbro.R
import com.yonas.focusbro.data.AppDatabase
import com.yonas.focusbro.data.TagBreakdown
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ProgressFragment : Fragment() {

    private lateinit var periodTabs: TabLayout
    private lateinit var totalFocusTime: TextView
    private lateinit var sessionsCompleted: TextView
    private lateinit var pieChart: PieChart
    private lateinit var shareButton: Button

    private val periods = listOf("Today", "This Week", "This Month", "This Year")
    private var currentPeriodIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_progress, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        periodTabs = view.findViewById(R.id.periodTabs)
        totalFocusTime = view.findViewById(R.id.totalFocusTime)
        sessionsCompleted = view.findViewById(R.id.sessionsCompleted)
        pieChart = view.findViewById(R.id.pieChart)
        shareButton = view.findViewById(R.id.shareButton)

        periods.forEach { periodTabs.addTab(periodTabs.newTab().setText(it)) }

        periodTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                currentPeriodIndex = tab?.position ?: 0
                loadData()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        shareButton.setOnClickListener {
            shareSummary()
        }

        loadData()
    }

    private fun loadData() {
        val (start, end) = getPeriodRange(currentPeriodIndex)

        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(requireContext()).sessionDao()

            // ✅ Use first() to get single value
            val totalMin = dao.getTotalMinutes(start, end).first()
            val total = totalMin ?: 0
            val hours = total / 60
            val mins = total % 60
            totalFocusTime.text = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

            val count = dao.getSessionCount(start, end).first()
            sessionsCompleted.text = count?.toString() ?: "0"

            val breakdown = dao.getTagBreakdown(start, end).first()
            updatePieChart(breakdown)
        }
    }

    private fun getPeriodRange(periodIndex: Int): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val end = calendar.timeInMillis

        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val todayStart = calendar.timeInMillis

        return when (periodIndex) {
            0 -> Pair(todayStart, end) // Today
            1 -> {
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                Pair(calendar.timeInMillis, end)
            }
            2 -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                Pair(calendar.timeInMillis, end)
            }
            3 -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                Pair(calendar.timeInMillis, end)
            }
            else -> Pair(todayStart, end)
        }
    }

    private fun updatePieChart(breakdown: List<TagBreakdown>) {
        if (breakdown.isEmpty()) {
            pieChart.clear()
            pieChart.setNoDataText("No data for this period")
            return
        }

        val entries = breakdown.map { PieEntry(it.total.toFloat(), it.tag) }
        val dataSet = PieDataSet(entries, "Focus by Tag")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
        dataSet.valueTextColor = resources.getColor(android.R.color.black)
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(true)
        dataSet.valueFormatter = PercentFormatter(pieChart)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.holeRadius = 40f
        pieChart.transparentCircleRadius = 45f
        pieChart.animateY(1000)
        pieChart.invalidate()
    }

    // ============================================
    // FIXED SHARE SUMMARY
    // ============================================
    private fun shareSummary() {
        lifecycleScope.launch {
            val (start, end) = getPeriodRange(currentPeriodIndex)
            val dao = AppDatabase.getInstance(requireContext()).sessionDao()

            // Get data once
            val totalMin = dao.getTotalMinutes(start, end).first() ?: 0
            val hours = totalMin / 60
            val mins = totalMin % 60
            val timeText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

            val sessionCount = dao.getSessionCount(start, end).first() ?: 0

            val breakdown = dao.getTagBreakdown(start, end).first()
            val tagText = if (breakdown.isNotEmpty()) {
                breakdown.joinToString("\n") { "${it.tag}: ${it.total} min" }
            } else {
                "No sessions recorded"
            }

            val periodLabel = periods[currentPeriodIndex]
            val shareText = """
                🚀 FocusBro Summary - $periodLabel
                ________________________________________________________________________________________________________________________________________________________________________________________________________________________
                🕒 Total Focus Time: $timeText
                🔥 Sessions Completed: $sessionCount
                🏷️ Breakdown by Tag:
                $tagText
            """.trimIndent()

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, shareText)
            }
            startActivity(Intent.createChooser(intent, "Share Summary"))
        }
    }
}