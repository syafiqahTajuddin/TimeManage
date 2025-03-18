package com.nursyafiqahtajuddin.timemanage.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.Habit
import java.util.Calendar

class HabitAdapter(
    private var habits: List<Habit>,
    private val onDayToggle: (Habit, Int, Boolean) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    class HabitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val habitTitle: TextView = view.findViewById(R.id.tvHabitName)
        val daysContainer: LinearLayout = view.findViewById(R.id.daysContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]

        holder.habitTitle.text = habit.habitTitle

        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val todayCalendar = Calendar.getInstance()
        todayCalendar.set(Calendar.HOUR_OF_DAY, 0)
        todayCalendar.set(Calendar.MINUTE, 0)
        todayCalendar.set(Calendar.SECOND, 0)
        todayCalendar.set(Calendar.MILLISECOND, 0)
        val today = todayCalendar.time

        holder.daysContainer.removeAllViews()

        days.forEachIndexed { index, day ->
            val dayIcon = ImageView(holder.itemView.context).apply {
                layoutParams = LinearLayout.LayoutParams(48, 48).apply { marginStart = 8 }

                setImageResource(
                    if (habit.habitCompletion.getOrNull(index) == true)
                        R.drawable.ic_check_circle_filled
                    else
                        R.drawable.ic_check_circle_outline
                )

                val habitDay = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_WEEK, index + 2)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time

                val isInFrequency = habit.habitFrequency.contains(day)

                when {
                    habitDay.before(today) -> {
                        isEnabled = true
                        setImageResource(
                            if (habit.habitCompletion.getOrNull(index) == true)
                                R.drawable.ic_check_circle_filled
                            else
                                R.drawable.ic_check_circle_outline
                        )
                        setOnClickListener {
                            Toast.makeText(
                                holder.itemView.context,
                                "You can't modify this day.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    habitDay == today -> {
                        if (isInFrequency){
                            isEnabled = true
                            setImageResource(
                                if (habit.habitCompletion.getOrNull(index) == true)
                                    R.drawable.ic_check_circle_filled
                                else
                                    R.drawable.ic_check_circle_outline
                            )
                            setOnClickListener {
                                val newCompletionState = habit.habitCompletion.getOrNull(index) != true
                                onDayToggle(habit, index, newCompletionState)
                            }
                        } else{

                            setImageResource(R.drawable.ic_check_circle_outline) // Keep it unselected
                            setOnClickListener {
                                Toast.makeText(
                                    holder.itemView.context,
                                    "This day is not in your frequency.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                    }

                    else -> {
                        isEnabled = true
                        setImageResource(R.drawable.ic_check_circle_outline)
                        setOnClickListener {
                            Toast.makeText(
                                holder.itemView.context,
                                "Wait for the next day to check this habit.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            holder.daysContainer.addView(dayIcon)
        }
    }

    override fun getItemCount(): Int = habits.size

    fun updateHabits(newHabits: List<Habit>) {
        this.habits = newHabits
        notifyDataSetChanged()
    }
}