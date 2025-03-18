package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.MainActivity
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentAddTaskBinding
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository
import java.util.*

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private val taskRepository = TaskRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var selectedPriority = ""
    private var selectedDeadline: Date? = null
    private var selectedNotificationDate: Date? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainActivity = requireActivity() as MainActivity

        mainActivity.findViewById<View>(R.id.tabLayout)?.visibility = View.GONE

        setupToolbar()

        setupPriorityDropdown()
        setupDatePickers()

        binding.createTaskButton.setOnClickListener {
            saveTask()
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupPriorityDropdown() {
        val priorities = listOf(
            "Important + Urgent",
            "Important + Not Urgent",
            "Not Important + Urgent",
            "Not Important + Not Urgent"
        )

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            priorities
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.priorityDropdown.adapter = adapter

        binding.priorityDropdown.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedPriority = priorities[position]
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedPriority = ""
            }
        }
    }

    private fun setupDatePickers() {
        binding.deadlineField.setOnClickListener {
            showDatePicker { date ->
                selectedDeadline = date
                binding.deadlineField.setText(dateToString(date))
            }
        }
        binding.notificationField.setOnClickListener {
            showDatePicker { date ->
                selectedNotificationDate = date
                binding.notificationField.setText(dateToString(date))
            }
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                }.time
                onDateSelected(selectedDate)
            },
            year,
            month,
            day
        ).show()
    }

    private fun saveTask() {
        val taskTitle = binding.taskTitleField.text.toString().trim()
        val pomodoroEstimated = binding.estimatedPomodorosField.text.toString().toIntOrNull()
        val userID = firebaseAuth.currentUser?.uid ?: ""

        android.util.Log.d("AddTaskFragment", "Task Title: $taskTitle")
        android.util.Log.d("AddTaskFragment", "Pomodoro Estimated: $pomodoroEstimated")
        android.util.Log.d("AddTaskFragment", "UserID: $userID")
        android.util.Log.d("AddTaskFragment", "Selected Priority: $selectedPriority")
        android.util.Log.d("AddTaskFragment", "Selected Deadline: $selectedDeadline")
        android.util.Log.d("AddTaskFragment", "Notification Date: $selectedNotificationDate")

        if (taskTitle.isEmpty() || userID.isEmpty() || pomodoroEstimated == null) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val newTask = Task(
            userID = userID,
            taskTitle = taskTitle,
            pomodoroEstimated = pomodoroEstimated,
            taskCreationDate = Date(),
            taskDeadline = selectedDeadline,
            notificationDate = selectedNotificationDate,
            taskPriority = selectedPriority,
            taskStatus = "Pending"
        )

        taskRepository.addTask(newTask) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show()
                scheduleNotification(newTask)
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to add task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun scheduleNotification(task: Task) {
        if (task.notificationDate != null && task.taskDeadline != null) {
            val notificationTime = task.taskDeadline!!.time - task.notificationDate!!.time // Calculate the notification time
            val notificationScheduler = TaskNotificationScheduler(requireContext())
            notificationScheduler.scheduleTaskNotification(task, notificationTime)
        }
    }

    private fun dateToString(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$day/$month/$year"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.findViewById<View>(R.id.tabLayout)?.visibility = View.VISIBLE
        _binding = null
    }
}