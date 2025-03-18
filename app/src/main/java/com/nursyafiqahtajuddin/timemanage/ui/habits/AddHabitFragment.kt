package com.nursyafiqahtajuddin.timemanage.ui.habits

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.data.models.Habit
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentAddHabitBinding
import com.nursyafiqahtajuddin.timemanage.repository.HabitRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddHabitFragment : Fragment() {

    private lateinit var binding: FragmentAddHabitBinding
    private val habitRepository = HabitRepository()
    private var selectedReminder: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etReminderTime.setOnClickListener {
            showTimePicker()
        }

        binding.btnDone.setOnClickListener {
            val name = binding.etHabitName.text.toString()
            val frequency = getSelectedDays()
            val description = binding.etNote.text.toString()

            if (name.isNotEmpty()) {
                val habit = Habit(
                    habitId = "",
                    habitTitle = name,
                    habitFrequency = frequency,
                    habitCompletion = MutableList(7) { false },
                    habitReminders = selectedReminder,
                    habitDescription = description,
                    userID = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                )

                saveHabit(habit)
            } else {
                Toast.makeText(requireContext(), "Please fill in the habit name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)

                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                selectedReminder = calendar.time
                binding.etReminderTime.setText(timeFormat.format(selectedReminder))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun saveHabit(habit: Habit) {
        lifecycleScope.launch {
            val frequency = getSelectedDays()
            val completionState = List(7) { day -> frequency.contains(getDayString(day)) }

            val habitToSave = habit.copy(
                habitCompletion = completionState.toMutableList(),
                habitFrequency = frequency,
                habitId = ""
            )
            val success = habitRepository.addHabit(habitToSave)
            if (success) {
                Toast.makeText(requireContext(), "Habit added successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to add habit", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDayString(dayIndex: Int): String {
        return when (dayIndex) {
            0 -> "Mon"
            1 -> "Tue"
            2 -> "Wed"
            3 -> "Thu"
            4 -> "Fri"
            5 -> "Sat"
            6 -> "Sun"
            else -> ""
        }
    }

    private fun getSelectedDays(): List<String> {
        val selectedDays = mutableListOf<String>()
        if (binding.btnMonday.isChecked) selectedDays.add("Mon")
        if (binding.btnTuesday.isChecked) selectedDays.add("Tue")
        if (binding.btnWednesday.isChecked) selectedDays.add("Wed")
        if (binding.btnThursday.isChecked) selectedDays.add("Thu")
        if (binding.btnFriday.isChecked) selectedDays.add("Fri")
        if (binding.btnSaturday.isChecked) selectedDays.add("Sat")
        if (binding.btnSunday.isChecked) selectedDays.add("Sun")

        if (selectedDays.isEmpty()) {
            Toast.makeText(requireContext(), "Please select at least one day", Toast.LENGTH_SHORT).show()
        }
        return selectedDays
    }

}