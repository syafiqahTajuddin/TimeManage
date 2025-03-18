package com.nursyafiqahtajuddin.timemanage.ui.reminders

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Reminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val reminders = mutableListOf<Reminder>()

    inner class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.reminderTitle)
        val messageTextView: TextView = itemView.findViewById(R.id.reminderMessage)
        val reminderTimeTextView: TextView = itemView.findViewById(R.id.reminderTime)
        val notificationStatusTextView: TextView = itemView.findViewById(R.id.notificationStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]
        holder.titleTextView.text = reminder.reminderTitle
        holder.messageTextView.text = reminder.reminderMessage
        holder.reminderTimeTextView.text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            .format(Date(reminder.reminderTime))

        if (reminder.reminderTime != null) {
            holder.notificationStatusTextView.text = "Notification set for: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(reminder.reminderTime))}"
        } else {
            holder.notificationStatusTextView.text = "No notification set yet"
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun submitList(newReminders: List<Reminder>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }
}