package com.nursyafiqahtajuddin.timemanage.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentAnalysisBinding
import com.nursyafiqahtajuddin.timemanage.ui.pomodoro.PomodoroViewModel

class AnalysisFragment : Fragment() {

    private lateinit var binding: FragmentAnalysisBinding
    private lateinit var analysisViewModel: AnalysisViewModel
    private lateinit var pomodoroViewModel: PomodoroViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAnalysisBinding.inflate(inflater, container, false)
        analysisViewModel = ViewModelProvider(this)[AnalysisViewModel::class.java]
        pomodoroViewModel = ViewModelProvider(this)[PomodoroViewModel::class.java]

        analysisViewModel.todayFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeToday.text = formatTime(time)
        }

        analysisViewModel.weeklyFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeThisWeek.text = formatTime(time)
        }

        analysisViewModel.monthlyFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeThisMonth.text = formatTime(time)
        }

        analysisViewModel.loadFocusTimes()

        setupObservers()
        setupDropdown()

        return binding.root
    }

    private fun setupObservers() {
        pomodoroViewModel.todayFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeToday.text = formatTime(time)
        }

        pomodoroViewModel.weeklyFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeThisWeek.text = formatTime(time)
        }

        pomodoroViewModel.monthlyFocusTime.observe(viewLifecycleOwner) { time ->
            binding.focusTimeThisMonth.text = formatTime(time)
        }
    }

    private fun setupDropdown() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.period_options,
            android.R.layout.simple_spinner_dropdown_item
        )
        binding.periodSpinner.adapter = adapter

        binding.periodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> showFragment(ReportWeeklyFragment())  // Weekly Report
                    1 -> showFragment(ReportMonthlyFragment()) // Monthly Report
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun showFragment(fragment: Fragment) {
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, fragment)
        }
    }

    private fun formatTime(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return "${hours}h ${mins}m"
    }
}
