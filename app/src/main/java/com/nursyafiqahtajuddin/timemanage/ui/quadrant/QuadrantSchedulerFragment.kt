package com.nursyafiqahtajuddin.timemanage.ui.quadrant

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentQuadrantSchedulerBinding
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository
import java.util.Calendar
import java.util.Date

class QuadrantSchedulerFragment : Fragment() {

    private var _binding: FragmentQuadrantSchedulerBinding? = null
    private val binding get() = _binding!!

    private lateinit var importantUrgentAdapter: QuadrantAdapter
    private lateinit var importantNotUrgentAdapter: QuadrantAdapter
    private lateinit var notImportantUrgentAdapter: QuadrantAdapter
    private lateinit var notImportantNotUrgentAdapter: QuadrantAdapter

    private val taskRepository = TaskRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuadrantSchedulerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupDropdown()
        loadTasks()
    }

    private fun setupDropdown() {
        val options = resources.getStringArray(R.array.generate_options)
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            options
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.dropdownSpinner.adapter = adapter

        binding.dropdownSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = options[position]
                    Log.d("Dropdown", "Selected: $selectedOption")

                    when (selectedOption) {
                        "All" -> loadTasks()
                        "Today" -> fetchTasksForToday()
                        "This Week" -> fetchTasksForThisWeek()
                        "This Month" -> fetchTasksForThisMonth()
                        "Custom Range" -> showCustomRangeDialog()
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupRecyclerViews() {
        importantUrgentAdapter = QuadrantAdapter { task ->
            Log.d("QuadrantScheduler", "Task clicked: ${task.taskTitle} (Important + Urgent)")
        }
        binding.importantUrgentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.importantUrgentRecyclerView.adapter = importantUrgentAdapter

        importantNotUrgentAdapter = QuadrantAdapter { task ->
            Log.d("QuadrantScheduler", "Task clicked: ${task.taskTitle} (Important + Not Urgent)")
        }
        binding.importantNotUrgentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.importantNotUrgentRecyclerView.adapter = importantNotUrgentAdapter

        notImportantUrgentAdapter = QuadrantAdapter { task ->
            Log.d("QuadrantScheduler", "Task clicked: ${task.taskTitle} (Not Important + Urgent)")
        }
        binding.notImportantUrgentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.notImportantUrgentRecyclerView.adapter = notImportantUrgentAdapter

        notImportantNotUrgentAdapter = QuadrantAdapter { task ->
            Log.d(
                "QuadrantScheduler",
                "Task clicked: ${task.taskTitle} (Not Important + Not Urgent)"
            )
        }
        binding.notImportantNotUrgentRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.notImportantNotUrgentRecyclerView.adapter = notImportantNotUrgentAdapter
    }

    private fun loadTasks() {
        val userID = firebaseAuth.currentUser?.uid
        if (userID != null) {
            taskRepository.getAllTasks(userID) { tasks ->
                categorizeTasks(tasks)
            }
        }
    }

    private fun fetchTasksForToday() {
        val calendarStart = Calendar.getInstance()
        calendarStart.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendarStart.time

        val calendarEnd = Calendar.getInstance()
        calendarEnd.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = calendarEnd.time

        Log.d("fetchTasksForToday", "Start=$startDate, End=$endDate")
        loadTasksForRange(startDate, endDate)
    }

    private fun fetchTasksForThisWeek() {
        val calendar = Calendar.getInstance()

        calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.time

        calendar.apply {
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = calendar.time

        Log.d("fetchTasksForThisWeek", "Start=$startDate, End=$endDate")
        loadTasksForRange(startDate, endDate)
    }

    private fun fetchTasksForThisMonth() {
        val calendar = Calendar.getInstance()

        calendar.apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startDate = calendar.time

        calendar.apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 0)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
        val endDate = calendar.time

        Log.d("fetchTasksForThisMonth", "Start=$startDate, End=$endDate")
        loadTasksForRange(startDate, endDate)
    }

    private fun showCustomRangeDialog() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            requireContext(),
            { _, startYear, startMonth, startDay ->
                val startDate = Calendar.getInstance()
                startDate.set(startYear, startMonth, startDay)

                DatePickerDialog(
                    requireContext(),
                    { _, endYear, endMonth, endDay ->
                        val endDate = Calendar.getInstance()
                        endDate.set(endYear, endMonth, endDay)

                        Log.d("CustomRange", "Start: ${startDate.time}, End: ${endDate.time}")
                        loadTasksForRange(startDate.time, endDate.time)
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadTasksForRange(startDate: Date, endDate: Date) {
        val userID = firebaseAuth.currentUser?.uid
        if (userID != null) {
            Log.d("loadTasksForRange", "Fetching tasks for range: Start=$startDate, End=$endDate")
            taskRepository.getTasksByTimeRange(userID, startDate, endDate) { tasks ->
                Log.d("loadTasksForRange", "Fetched ${tasks.size} tasks")
                categorizeTasks(tasks)
            }
        }
    }

    private fun categorizeTasks(tasks: List<Task>) {
        val importantUrgentTasks = tasks.filter { it.taskPriority == "Important + Urgent" }
        val importantNotUrgentTasks = tasks.filter { it.taskPriority == "Important + Not Urgent" }
        val notImportantUrgentTasks = tasks.filter { it.taskPriority == "Not Important + Urgent" }
        val notImportantNotUrgentTasks =
            tasks.filter { it.taskPriority == "Not Important + Not Urgent" }

        importantUrgentAdapter.submitList(importantUrgentTasks)
        importantNotUrgentAdapter.submitList(importantNotUrgentTasks)
        notImportantUrgentAdapter.submitList(notImportantUrgentTasks)
        notImportantNotUrgentAdapter.submitList(notImportantNotUrgentTasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}