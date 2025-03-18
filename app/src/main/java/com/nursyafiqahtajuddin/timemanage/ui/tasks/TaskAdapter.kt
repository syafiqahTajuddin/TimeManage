package com.nursyafiqahtajuddin.timemanage.ui.tasks

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository

class TaskAdapter(
    private val tasks: MutableList<Task> = mutableListOf(),
    private val onTaskClick: (Task) -> Unit,
    private val onTaskStatusChanged: (Int) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDetails: LinearLayout = itemView.findViewById(R.id.taskDetails)
        val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTitle.text = task.taskTitle

        // Check if the task is completed
        if (task.taskStatus == "Completed") {
            holder.taskTitle.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            holder.checkIcon.setImageResource(R.drawable.ic_check_circle_filled)
        } else {
            holder.taskTitle.paintFlags = 0
            holder.checkIcon.setImageResource(R.drawable.ic_check_circle_outline)
        }

        // Update the task status and notify progress
        holder.checkIcon.setOnClickListener {
            // Update task status to completed
            task.taskStatus = if (task.taskStatus == "Completed") "Pending" else "Completed"

            TaskRepository().updateTask(task) { success ->
                if (success) {
                    onTaskStatusChanged(position)

                    tasks.removeAt(position)
                    tasks.add(task)
                    notifyItemMoved(position, tasks.size - 1)
                    notifyDataSetChanged()
                }
            }
        }

        // Display additional task details
        val pomodoroCount: TextView = holder.taskDetails.findViewById(R.id.pomodoroCount)
        val deadlineDate: TextView = holder.taskDetails.findViewById(R.id.deadlineDate)
        val priorityIcon: ImageView = holder.taskDetails.findViewById(R.id.priorityIcon)

        pomodoroCount.text = task.pomodoroEstimated?.toString() ?: "0"
        deadlineDate.text = task.taskDeadline?.toString() ?: "No Deadline"

        when (task.taskPriority) {
            "Important + Urgent" -> priorityIcon.setImageResource(R.drawable.ic_flag)
            "Important + Not Urgent" -> priorityIcon.setImageResource(R.drawable.ic_flag)
            "Not Important + Urgent" -> priorityIcon.setImageResource(R.drawable.ic_flag)
            "Not Important + Not Urgent" -> priorityIcon.setImageResource(R.drawable.ic_flag)
            else -> priorityIcon.setImageResource(R.drawable.ic_flag)
        }

        holder.taskTitle.setOnClickListener {
            val action = TaskListFragmentDirections.actionTaskListFragmentToEditTaskFragment(task.taskID)
            it.findNavController().navigate(action)
        }

        holder.checkIcon.setOnClickListener {
            task.taskStatus = "Completed"
            val viewModel = ViewModelProvider(holder.itemView.context as FragmentActivity).get(TaskViewModel::class.java)
            viewModel.updateTaskStatus(task)

            tasks.removeAt(position)
            tasks.add(task)
            notifyItemMoved(position, tasks.size - 1)
            notifyDataSetChanged()

            TaskRepository().updateTask(task) { success ->
                if (success) {
                    tasks.removeAt(position)
                    tasks.add(task)
                    notifyItemMoved(position, tasks.size - 1)
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun getItemCount(): Int = tasks.size

    fun submitList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.taskID == newItem.taskID
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}