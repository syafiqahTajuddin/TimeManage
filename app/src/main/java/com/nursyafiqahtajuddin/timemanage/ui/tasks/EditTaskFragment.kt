package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.MainActivity
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentEditTaskBinding
import java.util.*

class EditTaskFragment : Fragment() {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!
    private val taskRepository = TaskRepository()
    private val args: EditTaskFragmentArgs by navArgs()

    private var selectedPriority = ""
    private var selectedDeadline: Date? = null
    private var selectedNotificationDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        hideTabLayout()

        loadTaskDetails(args.taskID)
        setupPriorityDropdown()
        setupDatePicker()

        binding.updateTaskButton.setOnClickListener {
            updateTask()
        }
    }

    private fun setupToolbar() {
        val toolbar = binding.toolbar
        toolbar.title = "Edit Task"
        toolbar.inflateMenu(R.menu.menu_edit_task)

        toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    showDeleteConfirmationDialog() // Show confirmation before deletion
                    true
                }
                else -> false
            }
        }

        toolbar.setNavigationIcon(R.drawable.ic_back)
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun showDeleteConfirmationDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Task")
            .setMessage("Are you sure you want to delete this task?")
            .setPositiveButton("Delete") { _, _ -> deleteTask() }
            .setNegativeButton("Cancel", null)
            .create()

        alertDialog.show()
    }

    private fun deleteTask() {
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        android.util.Log.d("EditTaskFragment", "Deleting task with ID: ${args.taskID}, userID: $userID")
        taskRepository.deleteTask(args.taskID) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Task deleted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to delete task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTaskDetails(taskID: String) {
        taskRepository.getTaskById(taskID) { task, success ->
            if (success && task != null) {
                binding.taskTitleField.setText(task.taskTitle)
                binding.estimatedPomodorosField.setText(task.pomodoroEstimated?.toString() ?: "")
                binding.deadlineField.setText(task.taskDeadline?.let { dateToString(it) } ?: "")

                selectedPriority = task.taskPriority
                selectedDeadline = task.taskDeadline
                selectedNotificationDate = task.notificationDate

                val priorities = resources.getStringArray(R.array.task_priorities)
                val priorityIndex = priorities.indexOf(task.taskPriority)
                if (priorityIndex >= 0) {
                    binding.priorityDropdown.setSelection(priorityIndex)
                }
            } else {
                Toast.makeText(requireContext(), "Failed to load task", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupPriorityDropdown() {
        val priorities = resources.getStringArray(R.array.task_priorities).toList()

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

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDatePicker() {
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

    private fun updateTask() {
        val taskTitle = binding.taskTitleField.text.toString().trim()
        val pomodoroEstimated = binding.estimatedPomodorosField.text.toString().toIntOrNull()
        val userID = FirebaseAuth.getInstance().currentUser?.uid
        val notificationDate = selectedNotificationDate

        if (taskTitle.isEmpty() || pomodoroEstimated == null || userID.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all required fields", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedTask = Task(
            taskID = args.taskID,
            userID = userID,
            taskTitle = taskTitle,
            pomodoroEstimated = pomodoroEstimated,
            taskDeadline = selectedDeadline,
            notificationDate = notificationDate,
            taskPriority = selectedPriority
        )

        taskRepository.updateTask(updatedTask) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Task updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), "Failed to update task", Toast.LENGTH_SHORT).show()
            }
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

    private fun hideTabLayout() {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.findViewById<View>(R.id.tabLayout)?.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = requireActivity() as MainActivity
        mainActivity.findViewById<View>(R.id.tabLayout)?.visibility = View.VISIBLE
        _binding = null
    }
}