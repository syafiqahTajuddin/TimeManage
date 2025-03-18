package com.nursyafiqahtajuddin.timemanage.ui.timetable

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.CalendarItem

class CalendarAdapter(
    private val calendarItems: List<CalendarItem>,
    private val onDateClick: (CalendarItem) -> Unit
) : RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder>() {

    inner class CalendarViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvTasks: TextView = view.findViewById(R.id.tvTasks)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_cell, parent, false)
        return CalendarViewHolder(view)
    }

    override fun onBindViewHolder(holder: CalendarViewHolder, position: Int) {
        val item = calendarItems[position]

        holder.tvDate.text = item.date

        holder.tvDate.setTextColor(
            if (item.isInCurrentMonth) holder.itemView.context.getColor(R.color.black)
            else holder.itemView.context.getColor(R.color.Grey)
        )

        holder.tvTasks.text = item.tasks.joinToString(", ")

        holder.itemView.setOnClickListener {
            onDateClick(item)
        }
    }

    override fun getItemCount(): Int = calendarItems.size

    fun getItems(): List<CalendarItem> = calendarItems
}