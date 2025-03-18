package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentTaskListBinding
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: TaskViewModel by viewModels()
    private val taskRepository = TaskRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        binding.addTaskButton.setOnClickListener {
            Log.d("TaskListFragment", "Add Task button clicked")
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        Log.d("TaskListFragment", "Add Task Button visibility: ${binding.addTaskButton.visibility}")

        taskViewModel._tasks.observe(viewLifecycleOwner) { tasks ->
            Log.d("TaskListFragment", "Tasks: $tasks")
            taskAdapter.submitList(tasks)
        }

        taskViewModel._progress.observe(viewLifecycleOwner) { progress ->
            updateProgressUI(progress)
        }

        taskViewModel.fetchTasks()

        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { task ->
            },
            onTaskStatusChanged = { position ->
            }
        )
        binding.taskRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.taskRecyclerView.adapter = taskAdapter
    }

    private fun loadTasks() {
        val userID = firebaseAuth.currentUser?.uid
        if (userID != null) {
            taskRepository.getAllTasks(userID) { tasks ->
                taskAdapter.submitList(tasks)
            }
        }
    }

    private fun updateProgressUI(progress: Int) {
        binding.progressIndicator.progress = progress
        binding.progressText.text = "$progress%"
        val completed = taskViewModel._completedTasks.value ?: 0
        val total = taskViewModel._tasks.value?.size ?: 0
        binding.totalTasks.text = "Total tasks: $total"
        binding.completedTasks.text = "Completed tasks: $completed"
        binding.pendingTasks.text = "Pending tasks: ${total - completed}"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}