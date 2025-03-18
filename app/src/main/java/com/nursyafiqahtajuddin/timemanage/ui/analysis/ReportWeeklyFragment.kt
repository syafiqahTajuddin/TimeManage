package com.nursyafiqahtajuddin.timemanage.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentReportWeeklyBinding
import com.nursyafiqahtajuddin.timemanage.ui.pomodoro.PomodoroViewModel

class ReportWeeklyFragment : Fragment() {

    private lateinit var binding: FragmentReportWeeklyBinding
    private lateinit var pomodoroViewModel: PomodoroViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportWeeklyBinding.inflate(inflater, container, false)
        pomodoroViewModel = ViewModelProvider(this)[PomodoroViewModel::class.java]

        setupObservers()
        return binding.root
    }

    private fun setupObservers() {
        pomodoroViewModel.weeklyFocusTime.observe(viewLifecycleOwner) { weeklyTime ->
            binding.totalFocusTime.text = formatTime(weeklyTime)
            updateBarChart(weeklyTime)
        }
    }

    private fun updateBarChart(weeklyFocusTime: Int) {
        val dailyTimes = distributeTimeOverDays(weeklyFocusTime)
        val bars = listOf(
            binding.barMon, binding.barTue, binding.barWed,
            binding.barThu, binding.barFri, binding.barSat, binding.barSun
        )

        for ((index, time) in dailyTimes.withIndex()) {
            val barHeight = (time * 4).coerceAtMost(200)
            bars[index].layoutParams.height = barHeight
            bars[index].requestLayout()
        }
    }

    private fun distributeTimeOverDays(weeklyTime: Int): List<Int> {
        return listOf(weeklyTime / 7, weeklyTime / 6, weeklyTime / 5, weeklyTime / 4, weeklyTime / 3, weeklyTime / 2, weeklyTime)
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return String.format("%dh %dm", hours, mins)
    }
}