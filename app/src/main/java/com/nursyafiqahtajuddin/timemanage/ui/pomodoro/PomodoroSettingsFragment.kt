package com.nursyafiqahtajuddin.timemanage.ui.pomodoro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentPomodoroSettingsBinding

class PomodoroSettingsFragment : Fragment() {

    private lateinit var binding: FragmentPomodoroSettingsBinding
    private lateinit var pomodoroViewModel: PomodoroViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPomodoroSettingsBinding.inflate(inflater, container, false)
        pomodoroViewModel = ViewModelProvider(this)[PomodoroViewModel::class.java]
        navController = findNavController()

        pomodoroViewModel.settings.observe(viewLifecycleOwner) { settings ->
            binding.etPomodoroTime.setText(settings.focusDuration.toString())
            binding.etShortBreakTime.setText(settings.shortBreakDuration.toString())
            binding.etLongBreakTime.setText(settings.longBreakDuration.toString())
        }

        binding.btnSaveSettings.setOnClickListener {
            val focusDuration = binding.etPomodoroTime.text.toString().toIntOrNull() ?: 25
            val shortBreakDuration = binding.etShortBreakTime.text.toString().toIntOrNull() ?: 5
            val longBreakDuration = binding.etLongBreakTime.text.toString().toIntOrNull() ?: 15

            val userID = pomodoroViewModel.currentUserID ?: return@setOnClickListener

            pomodoroViewModel.saveSettings(userID, focusDuration, shortBreakDuration, longBreakDuration) { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Settings saved successfully!", Toast.LENGTH_SHORT).show()

                    findNavController().navigate(R.id.action_pomodoroSettingsFragment_to_pomodoroFragment)
                } else {
                    Toast.makeText(requireContext(), "Failed to save settings.", Toast.LENGTH_SHORT).show()
                }
            }
        }

        pomodoroViewModel.loadSettings()

        return binding.root
    }
}