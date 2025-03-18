package com.nursyafiqahtajuddin.timemanage.ui.analysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R

class MonthlyReportCalendarAdapter(private val dailyTimes: List<Int?>) :
    RecyclerView.Adapter<MonthlyReportCalendarAdapter.DayViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calendar_day, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = dailyTimes[position]
        holder.bind(day, if (day != null) dailyTimes[position] ?: 0 else -1)
    }

    override fun getItemCount(): Int = dailyTimes.size

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayNumber: TextView = itemView.findViewById(R.id.dayText)
        private val focusTimeText: TextView = itemView.findViewById(R.id.focusTimeText)

        fun bind(day: Int?, focusMinutes: Int) {
            if (day != null) {
                dayNumber.text = day.toString()
                focusTimeText.text = if (focusMinutes > 0) "$focusMinutes m" else "0m"
                itemView.visibility = View.VISIBLE
            } else {
                dayNumber.text = ""
                itemView.visibility = View.INVISIBLE
            }
        }
    }
}