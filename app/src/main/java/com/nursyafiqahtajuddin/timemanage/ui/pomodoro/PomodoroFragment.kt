package com.nursyafiqahtajuddin.timemanage.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentPomodoroBinding
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository

class PomodoroFragment : Fragment() {

    private lateinit var binding: FragmentPomodoroBinding
    private lateinit var pomodoroViewModel: PomodoroViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroBinding.inflate(inflater, container, false)
        pomodoroViewModel = ViewModelProvider(this)[PomodoroViewModel::class.java]

        pomodoroViewModel.settings.observe(viewLifecycleOwner) { settings ->
            updateTimerUI(settings.focusDuration, settings.shortBreakDuration)
        }

        pomodoroViewModel.timeLeft.observe(viewLifecycleOwner) { timeLeft ->
            updateTimerDisplay(timeLeft)
        }

        pomodoroViewModel.isBreak.observe(viewLifecycleOwner) { isBreak ->
            updateTitle(isBreak)
        }

        pomodoroViewModel.settings.observe(viewLifecycleOwner) { settings ->
            updateTimerUI(settings.focusDuration, settings.shortBreakDuration)
            binding.tvSessionInfo.text = "Sessions Completed: ${settings.sessionNumber}"
        }

        pomodoroViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            updateButtonStates(isRunning, pomodoroViewModel.isPaused)
        }

        binding.btnStartPause.setOnClickListener {
            if (pomodoroViewModel.isRunning.value == true) {
                pomodoroViewModel.pauseTimer()
            } else {
                val pomodoroTime = pomodoroViewModel.settings.value?.focusDuration ?: 25
                pomodoroViewModel.startTimer(pomodoroTime * 60)
            }
        }

        binding.btnStop.setOnClickListener {
            if (pomodoroViewModel.selectedTaskID != null) {
                val pomodoroTime = pomodoroViewModel.settings.value?.focusDuration ?: 25
                pomodoroViewModel.resetTimer(pomodoroTime * 60)
            } else {
                val pomodoroTime = pomodoroViewModel.settings.value?.focusDuration ?: 25
                pomodoroViewModel.resetTimer(pomodoroTime * 60)
                pomodoroViewModel.updateSessionNumber(0)
            }
        }

        binding.btnContinue.setOnClickListener {
            pomodoroViewModel.resumeTimer()
        }

        binding.ivSettings.setOnClickListener {
            findNavController().navigate(R.id.action_pomodoroFragment_to_pomodoroSettingsFragment)
        }

        setupTaskDropdown()
        return binding.root
    }

    private fun setupTaskDropdown() {
        val taskRepository = TaskRepository()
        taskRepository.getAllTasks(pomodoroViewModel.currentUserID!!) { tasks ->
            val taskTitles = tasks.map { it.taskTitle }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, taskTitles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.taskDropdown.adapter = adapter

            binding.taskDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    pomodoroViewModel.selectedTaskID = tasks[position].taskID
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    pomodoroViewModel.selectedTaskID = null
                }
            }
        }
    }

    private fun updateTimerUI(focusTime: Int, shortBreakTime: Int) {
        val minutes = (pomodoroViewModel.timeLeft.value ?: 0) / 60
        val seconds = (pomodoroViewModel.timeLeft.value ?: 0) % 60
        binding.cardTimer.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun updateTimerDisplay(timeLeft: Int) {
        val minutes = (timeLeft / 60)
        val seconds = (timeLeft % 60)
        binding.cardTimer.text = String.format("%02d:%02d", minutes, seconds)

        if (timeLeft == 0) {
            if (pomodoroViewModel.isBreak.value == true) {
                val updatedSessionNumber = (pomodoroViewModel.settings.value?.sessionNumber ?: 0) + 1
                binding.tvSessionInfo.text = "Sessions Completed: $updatedSessionNumber"

                Toast.makeText(requireContext(), "Pomodoro session completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateTitle(isBreak: Boolean) {
        if (isBreak) {
            val currentSession = pomodoroViewModel.settings.value?.sessionNumber ?: 0
            val totalSessions = pomodoroViewModel.settings.value?.totalSessions ?: 4
            binding.tvTitle.text = if (currentSession % totalSessions == 0) {
                "Time for Long Break"
            } else {
                "Time for Short Break"
            }
        } else {
            binding.tvTitle.text = "Time for Focus"
        }
    }

    private fun updateButtonStates(isRunning: Boolean, isPaused: Boolean) {
        when {
            isRunning -> {
                binding.btnStartPause.text = "Pause"
                binding.btnStartPause.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                binding.btnContinue.visibility = View.GONE
            }
            isPaused -> {
                binding.btnStartPause.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE
                binding.btnContinue.visibility = View.VISIBLE
            }
            else -> {
                binding.btnStartPause.text = "Start"
                binding.btnStartPause.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                binding.btnContinue.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pomodoroViewModel.loadSettings()
    }

}