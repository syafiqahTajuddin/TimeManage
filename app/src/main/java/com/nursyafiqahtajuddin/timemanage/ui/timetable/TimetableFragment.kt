package com.nursyafiqahtajuddin.timemanage.ui.timetable

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.nursyafiqahtajuddin.timemanage.R
import com.nursyafiqahtajuddin.timemanage.data.models.CalendarItem
import com.nursyafiqahtajuddin.timemanage.data.models.Task
import com.nursyafiqahtajuddin.timemanage.databinding.FragmentTimetableBinding
import com.nursyafiqahtajuddin.timemanage.repository.TaskRepository
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.*
import android.os.Environment
import androidx.core.app.NotificationCompat

class TimetableFragment : Fragment() {

    private lateinit var binding: FragmentTimetableBinding
    private lateinit var calendarAdapter: CalendarAdapter
    private val taskRepository = TaskRepository()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTimetableBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar(LocalDate.now().year, LocalDate.now().monthValue)

        binding.tvYear.setOnClickListener {
            showYearPickerDialog()
        }

        binding.tvMonth.setOnClickListener {
            showMonthPickerDialog()
        }

        binding.btnExportToPDF.setOnClickListener {
            lifecycleScope.launch {
                exportTimetableToPDF()
            }
        }
    }

    private fun setupCalendar(year: Int, month: Int) {
        val userID = firebaseAuth.currentUser?.uid
        if (userID != null) {
            val startDate = LocalDate.of(year, month, 1).toDate()
            val endDate = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).toDate()

            taskRepository.getTasksByTimeRange(userID, startDate, endDate) { tasks ->
                val calendarItems = generateCalendarItems(year, month, tasks)
                calendarAdapter = CalendarAdapter(calendarItems) { calendarItem ->
                    showTaskDetails(calendarItem)
                }

                binding.calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
                binding.calendarRecyclerView.adapter = calendarAdapter

                val monthName = YearMonth.of(year, month).month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                binding.tvMonth.text = monthName.uppercase()
                binding.tvYear.text = year.toString()
            }
        }
    }

    private fun generateCalendarItems(year: Int, month: Int, tasks: List<Task>): List<CalendarItem> {
        val items = mutableListOf<CalendarItem>()
        val firstDayOfMonth = LocalDate.of(year, month, 1)
        val daysInMonth = YearMonth.of(year, month).lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

        for (i in 0 until startDayOfWeek) {
            items.add(CalendarItem("", false, emptyList()))
        }

        for (day in 1..daysInMonth) {
            val currentDate = LocalDate.of(year, month, day)
            val dateTasks = tasks.filter { task ->
                task.taskDeadline?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate() == currentDate
            }

            items.add(CalendarItem(day.toString(), true, dateTasks.map { it.taskTitle }))
        }

        while (items.size % 7 != 0) {
            items.add(CalendarItem("", false, emptyList()))
        }

        return items
    }

    private fun showTaskDetails(calendarItem: CalendarItem) {
        if (calendarItem.tasks.isNotEmpty()) {
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Tasks for ${calendarItem.date}")
                .setItems(calendarItem.tasks.toTypedArray()) { _, index ->
                    Toast.makeText(requireContext(), "Clicked on task: ${calendarItem.tasks[index]}", Toast.LENGTH_SHORT).show()
                }
                .setPositiveButton("Close", null)
                .create()
            dialog.show()
        } else {
            Toast.makeText(requireContext(), "No tasks for this date.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showMonthPickerDialog() {
        val months = (1..12).map { YearMonth.of(2023, it).month.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
        val currentMonthIndex = LocalDate.now().monthValue - 1

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Month")
            .setSingleChoiceItems(months.toTypedArray(), currentMonthIndex) { dialog, which ->
                val selectedMonth = which + 1
                setupCalendar(LocalDate.now().year, selectedMonth)
                dialog.dismiss()
            }
            .show()
    }

    private fun showYearPickerDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_year_picker, null)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)

        yearPicker.minValue = 2000
        yearPicker.maxValue = 2100
        yearPicker.value = LocalDate.now().year

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        builder.setTitle("Select Year")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val selectedYear = yearPicker.value
                setupCalendar(selectedYear, LocalDate.now().monthValue)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportTimetableToPDF() {
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdir()

            val pdfFile = File(downloadsDir, "Timetable.pdf")
            val pdfWriter = PdfWriter(FileOutputStream(pdfFile))
            val pdfDocument = com.itextpdf.kernel.pdf.PdfDocument(pdfWriter)
            val document = Document(pdfDocument)

            document.add(Paragraph("Timetable for ${binding.tvMonth.text} ${binding.tvYear.text}").setBold())

            val table = Table(7)
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach {
                table.addCell(it)
            }

            val calendarItems = calendarAdapter.getItems()
            calendarItems.forEach { item ->
                val cellContent = if (item.date.isEmpty()) "" else "${item.date}\n${item.tasks.joinToString("\n")}"
                table.addCell(cellContent)
            }
            document.add(table)
            document.close()

            showPDFNotification(pdfFile)
            Toast.makeText(requireContext(), "PDF saved to Downloads: ${pdfFile.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Failed to export timetable: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPDFNotification(pdfFile: File) {
        val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "timetable_export_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "PDF Exports",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for exported PDFs"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(requireContext(), channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("PDF Exported")
            .setContentText("Timetable saved to Downloads: ${pdfFile.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

    private fun LocalDate.toDate(): Date =
        Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant())

}