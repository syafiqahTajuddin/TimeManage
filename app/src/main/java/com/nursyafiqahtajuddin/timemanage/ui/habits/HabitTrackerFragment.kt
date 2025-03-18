package com.nursyafiqahtajuddin.timemanage.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Habit
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentHabitTrackerBinding
import com.nursyafiqahtajuddin.timemanage.repository.HabitRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HabitTrackerFragment : Fragment() {

    private lateinit var binding: FragmentHabitTrackerBinding
    private lateinit var habitAdapter: HabitAdapter
    private val habitRepository = HabitRepository()
    private var currentWeekStart: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHabitTrackerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupAddHabitButton()
        setupWeekNavigation()
        loadHabits()
    }

    private fun setupRecyclerView() {
        binding.habitRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        habitAdapter = HabitAdapter(listOf()) { habit, dayIndex, newCompletionState ->
            updateHabitCompletion(habit, dayIndex, newCompletionState)
        }
        binding.habitRecyclerView.adapter = habitAdapter
    }

    private fun setupAddHabitButton() {
        binding.btnAddHabit.setOnClickListener {
            findNavController().navigate(R.id.action_habitTrackerFragment_to_addHabitFragment)
        }
    }

    private fun setupWeekNavigation() {
        binding.btnPreviousWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekDisplay()
            loadHabits()
        }

        binding.btnNextWeek.setOnClickListener {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekDisplay()
            loadHabits()
        }

        updateWeekDisplay()
    }

    private fun updateWeekDisplay() {
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        currentWeekStart.firstDayOfWeek = Calendar.MONDAY
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val start = currentWeekStart.time
        val endOfWeek = Calendar.getInstance().apply {
            time = start
            add(Calendar.DAY_OF_YEAR, 6)
        }.time

        binding.tvWeekRange.text = "${dateFormat.format(start)} - ${dateFormat.format(endOfWeek)}"
    }

    private fun loadHabits() {
        lifecycleScope.launch {
            try {
                val allHabits = habitRepository.getAllHabits()

                allHabits.forEach { habit ->
                    if (habit.habitCompletion.size != 7) {
                        habit.habitCompletion.clear()
                        repeat(7) { habit.habitCompletion.add(false) }
                    }
                }

                val filteredHabits = filterHabitsForCurrentWeek(allHabits)
                if (filteredHabits.isNotEmpty()) {
                    habitAdapter.updateHabits(filteredHabits)
                    updateProgressIndicators(filteredHabits)
                } else {
                    Toast.makeText(requireContext(), "No habits found for this week", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load habits", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun filterHabitsForCurrentWeek(habits: List<Habit>): List<Habit> {
        val daysInWeek = getDaysInWeek(currentWeekStart)
        return habits.filter { habit ->
            habit.habitFrequency.any { it in daysInWeek }
        }
    }

    private fun getDaysInWeek(start: Calendar): List<String> {
        val days = mutableListOf<String>()
        val dayFormatter = SimpleDateFormat("EEE", Locale.getDefault())

        val tempCalendar = start.clone() as Calendar
        tempCalendar.firstDayOfWeek = Calendar.MONDAY

        for (i in 0..6) {
            days.add(dayFormatter.format(tempCalendar.time))
            tempCalendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return days
    }

    private fun updateHabitCompletion(habit: Habit, dayIndex: Int, newCompletionState: Boolean) {
        lifecycleScope.launch {
            val updatedCompletion = habit.habitCompletion.toMutableList()
            updatedCompletion[dayIndex] = newCompletionState
            val success = habitRepository.updateHabitCompletion(habit.habitId, updatedCompletion)
            if (success) {
                loadHabits()
            } else {
                Toast.makeText(requireContext(), "Failed to update habit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProgressIndicators(habits: List<Habit>) {
        val totalHabitsForToday = habits.count { habit ->
            val today = SimpleDateFormat("EEE", Locale.getDefault()).format(Date())
            habit.habitFrequency.contains(today)
        }

        val completedHabitsForToday = habits.count { habit ->
            val todayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 2
            habit.habitFrequency.contains(SimpleDateFormat("EEE", Locale.getDefault()).format(Date())) &&
                    habit.habitCompletion.getOrNull(todayIndex) == true
        }

        val totalCompletedDays = habits.sumOf { habit -> habit.habitCompletion.count { it } }
        val totalTrackedDays = habits.size * 7

        val dailyRate = if (totalHabitsForToday > 0) (completedHabitsForToday * 100) / totalHabitsForToday else 0
        val overallProgress = if (totalTrackedDays > 0) (totalCompletedDays * 100) / totalTrackedDays else 0

        binding.tvOverallProgress.text = "$overallProgress%"
        binding.overallProgress.setProgress(overallProgress)

        binding.tvDailyRateProgress.text = "$dailyRate%"
        binding.dailyRateProgress.setProgress(dailyRate)
    }
}