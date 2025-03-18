package com.nursyafiqahtajuddin.timemanage.ui.analysis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R

class MonthlyReportAdapter(
    private val days: List<String>,
    private val focusedDays: List<Int>
) : RecyclerView.Adapter<MonthlyReportAdapter.MonthlyViewHolder>() {

    inner class MonthlyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayText: TextView = itemView.findViewById(R.id.dayText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthlyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_calendar_day, parent, false)
        return MonthlyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthlyViewHolder, position: Int) {
        val day = days[position]
        holder.dayText.text = day

        if (day.isNotEmpty() && day.toInt() in focusedDays) {
            holder.dayText.background = ContextCompat.getDrawable(holder.itemView.context, R.drawable.circle_background)
            holder.dayText.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.red))
        }
    }

    override fun getItemCount(): Int = days.size
}