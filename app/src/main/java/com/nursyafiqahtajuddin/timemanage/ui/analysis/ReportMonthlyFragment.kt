package com.nursyafiqahtajuddin.timemanage.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentReportMonthlyBinding
import com.nursyafiqahtajuddin.timemanage.ui.tasks.TaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ReportMonthlyFragment : Fragment() {

    private var _binding: FragmentReportMonthlyBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel: TaskViewModel by activityViewModels()
    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private lateinit var calendarAdapter: CalendarAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportMonthlyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMonthNavigation()
        setupCalendar()
    }

    private fun setupMonthNavigation() {
        binding.prevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateMonthTitle()
            updateCalendar()
        }

        binding.nextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateMonthTitle()
            updateCalendar()
        }
        updateMonthTitle()
    }

    private fun updateMonthTitle() {
        binding.monthTitle.text = dateFormat.format(calendar.time)
    }

    private fun setupCalendar() {
        calendarAdapter = CalendarAdapter(emptyList())
        binding.calendarRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 7)
            adapter = calendarAdapter
        }
        updateCalendar()
    }

    private fun updateCalendar() {
        val firstDayOfMonth = getFirstDayOfMonth(calendar)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
        val tasks = taskViewModel._tasks.value ?: emptyList()

        val calendarItems = mutableListOf<CalendarItem>()

        for (i in 1 until firstDayOfWeek) {
            calendarItems.add(CalendarItem.Empty)
        }

        for (day in 1..daysInMonth) {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_MONTH, day)
            val currentDay = calendar.time
            val tasksForDay = tasks.filter { isSameDay(it.taskDeadline, currentDay) }
            val totalFocusTime = tasksForDay.sumOf { it.pomodoroEstimated ?: 0 }
            calendarItems.add(CalendarItem.Day(day, totalFocusTime))
        }

        calendarAdapter.updateItems(calendarItems)
    }

    private fun getFirstDayOfMonth(calendar: Calendar): Calendar {
        val firstDay = calendar.clone() as Calendar
        firstDay.set(Calendar.DAY_OF_MONTH, 1)
        return firstDay
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = date1
        cal2.time = date2
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

sealed class CalendarItem {
    object Empty : CalendarItem()
    data class Day(val day: Int, val focusTime: Int) : CalendarItem()
}

class CalendarAdapter(private var items: List<CalendarItem>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    fun updateItems(newItems: List<CalendarItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_DAY -> {
                val view = inflater.inflate(R.layout.item_calendar_day, parent, false)
                DayViewHolder(view)
            }
            else -> {
                val view = inflater.inflate(R.layout.item_empty, parent, false)
                EmptyViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is CalendarItem.Day -> (holder as DayViewHolder).bind(item)
            is CalendarItem.Empty -> (holder as EmptyViewHolder).bind()
        }
    }

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is CalendarItem.Day -> VIEW_TYPE_DAY
            is CalendarItem.Empty -> VIEW_TYPE_EMPTY
        }
    }

    companion object {
        private const val VIEW_TYPE_DAY = 1
        private const val VIEW_TYPE_EMPTY = 2
    }

    class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val dayText: TextView = itemView.findViewById(R.id.dayText)
        private val focusTimeText: TextView = itemView.findViewById(R.id.focusTimeText)

        fun bind(item: CalendarItem.Day) {
            dayText.text = item.day.toString()
            focusTimeText.text = item.focusTime.toString()
        }
    }

    class EmptyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind() {
        }
    }
}