package com.example.honk.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.NestedScrollView


class CalendarFragment : Fragment() {

    private lateinit var calendarGrid: GridView
    private lateinit var monthLabel: TextView
    private lateinit var reminderList: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter

    private val reminders = mutableListOf<Reminder>()
    private var selectedDate: String? = null
    private var currentCalendar: Calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar_fragment, container, false)

        // --- Filter button ---
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener { showFilterDialog() }

        // --- Setup calendar views ---
        calendarGrid = view.findViewById(R.id.calendarGrid)
        monthLabel = view.findViewById(R.id.monthLabel)

        updateCalendar()

        // --- Month navigation buttons ---
        view.findViewById<View>(R.id.prevMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        view.findViewById<View>(R.id.nextMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        // --- Day click listener ---
        calendarGrid.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val day = position + 1
                val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                selectedDate = "${sdf.format(currentCalendar.time)}-${String.format("%02d", day)}"
                view.findViewById<TextView>(R.id.selectedDateLabel).text = "$day ${monthLabel.text}"
                updateReminderList()
            }

        // --- RecyclerView setup ---
        reminderList = view.findViewById(R.id.reminderList)
        reminderAdapter = ReminderAdapter(
            onReminderClick = { reminder ->
                showEditReminderDialog(reminder)
            },
            onDeleteClick = { reminder ->
                reminders.remove(reminder)
                updateReminderList()
            }
        )

        reminderList.layoutManager = LinearLayoutManager(requireContext())
        reminderList.adapter = reminderAdapter

        // --- FAB (add reminder) ---
        val fabAdd = view.findViewById<View>(R.id.fab_add_reminder)
        fabAdd.setOnClickListener { showAddReminderDialog() }

        return view
    }

    // --- Filter popup dialog ---
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

    // --- Calendar update logic ---
    private fun updateCalendar() {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        monthLabel.text = monthFormat.format(currentCalendar.time)
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = (1..daysInMonth).map { it.toString() }
        calendarGrid.adapter = DaysAdapter(days)
    }

    // --- Add Reminder dialog ---
    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val reminderText = dialogView.findViewById<EditText>(R.id.reminderText)
        val reminderTime = dialogView.findViewById<EditText>(R.id.reminderTime)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addReminderButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        addButton.setOnClickListener {
            val date = selectedDate
            val category = categorySpinner.selectedItem.toString()
            if (date != null && reminderText.text.isNotBlank()) {
                val reminder = Reminder(
                    date = date,
                    time = reminderTime.text.toString(),
                    text = reminderText.text.toString(),
                    category = category
                )
                reminders.add(reminder)
                updateReminderList()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Select a date first", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // --- Update visible reminders ---
    private fun updateReminderList() {
        val currentDate = selectedDate ?: return
        val filtered = reminders.filter { it.date == currentDate }
        reminderAdapter.setReminders(filtered)
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val reminderText = dialogView.findViewById<EditText>(R.id.reminderText)
        val reminderTime = dialogView.findViewById<EditText>(R.id.reminderTime)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addReminderButton)

        // Pre-fill existing values
        reminderText.setText(reminder.text)
        reminderTime.setText(reminder.time)
        addButton.text = "Save Changes"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Delete") { _, _ ->
                reminders.remove(reminder)
                updateReminderList()
            }
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        addButton.setOnClickListener {
            reminder.text = reminderText.text.toString()
            reminder.time = reminderTime.text.toString()
            reminder.category = categorySpinner.selectedItem.toString()
            updateReminderList()
            dialog.dismiss()
        }

        dialog.show()
    }

    // --- Adapter for calendar day cells ---
    inner class DaysAdapter(private val days: List<String>) : BaseAdapter() {
        override fun getCount() = days.size
        override fun getItem(position: Int) = days[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_day, parent, false)
            val dayText = view.findViewById<TextView>(R.id.dayText) ?: view as TextView
            dayText.text = days[position]

            val today = Calendar.getInstance()
            if (currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                days[position].toInt() == today.get(Calendar.DAY_OF_MONTH)
            ) {
                dayText.setBackgroundResource(R.drawable.day_cell_today_bg)
            } else {
                dayText.setBackgroundResource(R.drawable.day_cell_bg)
            }

            return view
        }
    }

    // --- Adapter for reminders list ---
    inner class ReminderAdapter(
        private val onReminderClick: (Reminder) -> Unit,
        private val onDeleteClick: (Reminder) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        private var reminderItems = listOf<Reminder>()

        fun setReminders(newReminders: List<Reminder>) {
            reminderItems = newReminders
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.reminderText)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

            init {
                view.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onReminderClick(reminderItems[pos])
                    }
                }

                deleteButton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteClick(reminderItems[pos])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reminder, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = reminderItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val r = reminderItems[position]
            holder.text.text = "${r.time} – ${r.text} (${r.category})"

            val color = when (r.category.uppercase()) {
                "WORK" -> 0xFF03A9F4.toInt()
                "SCHOOL" -> 0xFF4CAF50.toInt()
                "PET" -> 0xFFF44336.toInt()
                "HOME" -> 0xFF9C27B0.toInt()
                else -> 0xFF888888.toInt()
            }
            holder.text.setTextColor(color)
        }
    }

}
