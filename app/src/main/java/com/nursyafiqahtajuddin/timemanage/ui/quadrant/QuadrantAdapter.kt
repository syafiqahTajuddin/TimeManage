package com.nursyafiqahtajuddin.timemanage.ui.quadrant

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Task

class QuadrantAdapter(
    private val tasks: MutableList<Task> = mutableListOf(),
    private val onTaskClick: (Task) -> Unit
) : RecyclerView.Adapter<QuadrantAdapter.QuadrantViewHolder>() {

    inner class QuadrantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuadrantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_quadrant, parent, false)
        return QuadrantViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuadrantViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskTitle.text = task.taskTitle
    }

    override fun getItemCount(): Int = tasks.size

    fun submitList(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }
}