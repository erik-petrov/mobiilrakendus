package com.example.honk.ui.calendar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.data.TaskRepository
import com.example.honk.model.Reminder
import com.example.honk.ui.categories.CategoryViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import com.example.honk.ui.dialogs.TaskDialog

class CalendarFragment : Fragment() {

    private lateinit var calendarGrid: GridView
    private lateinit var monthLabel: TextView
    private lateinit var selectedDateLabel: TextView
    private lateinit var reminderList: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter

    private lateinit var categoryViewModel: CategoryViewModel

    private var selectedDate: String? = null
    private var currentCalendar: Calendar = Calendar.getInstance()

    // -------- Lifecycle --------

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar_fragment, container, false)

        // Shared categories between screens
        categoryViewModel =
            ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        // Views
        calendarGrid = view.findViewById(R.id.calendarGrid)
        monthLabel = view.findViewById(R.id.monthLabel)
        selectedDateLabel = view.findViewById(R.id.selectedDateLabel)
        reminderList = view.findViewById(R.id.reminderList)

        // Filter button (cosmetic for now)
        view.findViewById<ImageButton>(R.id.filterButton)
            .setOnClickListener { showFilterDialog() }

        // Calendar navigation
        view.findViewById<View>(R.id.prevMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        view.findViewById<View>(R.id.nextMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        // Set up month & grid
        updateCalendar()

        // RecyclerView setup
        reminderAdapter = ReminderAdapter(
            onReminderClick = { reminder ->
                TaskDialog.show(
                    fragment = this,
                    existing = reminder,
                    onSave = { TaskRepository.updateTask(it) },
                    onDelete = { TaskRepository.deleteTask(reminder) }
                )
            },
            onDeleteClick = { reminder ->
                TaskRepository.deleteTask(reminder)
            }
        )
        reminderList.layoutManager = LinearLayoutManager(requireContext())
        reminderList.adapter = reminderAdapter

        // Default: select TODAY, using dd.MM.yyyy to match date pickers
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        selectedDate = dateFormat.format(currentCalendar.time)
        val todayDay = currentCalendar.get(Calendar.DAY_OF_MONTH)
        selectedDateLabel.text = "$todayDay ${monthLabel.text}"

        updateReminderList()

        // Day click listener
        calendarGrid.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val adapter = calendarGrid.adapter as DaysAdapter
                val dayInfo = adapter.getDayInfo(position) ?: return@OnItemClickListener

                // Only allow selecting current month days
                if (!dayInfo.isCurrentMonth) return@OnItemClickListener

                val clickedCal = currentCalendar.clone() as Calendar
                clickedCal.set(Calendar.DAY_OF_MONTH, dayInfo.day.toInt())

                val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                selectedDate = sdf.format(clickedCal.time)

                selectedDateLabel.text = "${dayInfo.day} ${monthLabel.text}"
                updateReminderList()
            }

        // FAB – add reminder
        view.findViewById<View>(R.id.fab_add_reminder)
            .setOnClickListener {
                TaskDialog.show(
                    fragment = this,
                    presetDate = selectedDate,
                    onSave = { TaskRepository.addTask(it) }
                )
            }


        // Observe global tasks and refresh list when anything changes
        TaskRepository.tasks.observe(viewLifecycleOwner) {
            updateReminderList()
        }

        TaskRepository.tasks.observe(viewLifecycleOwner) {
            updateCalendar()
        }


        return view
    }

    private fun openDatePicker(onDateSelected: (String) -> Unit) {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(requireContext(), { _, y, m, d ->
            val formatted = "%02d.%02d.%d".format(d, m + 1, y) // dd.MM.yyyy
            onDateSelected(formatted)
        }, year, month, day).show()
    }

    // -------- Filter dialog (still cosmetic) --------

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val checkAll = dialogView.findViewById<CheckBox>(R.id.checkAll)
        val checkWork = dialogView.findViewById<CheckBox>(R.id.checkWork)
        val checkPet = dialogView.findViewById<CheckBox>(R.id.checkPet)
        val checkSchool = dialogView.findViewById<CheckBox>(R.id.checkSchool)
        val checkHome = dialogView.findViewById<CheckBox>(R.id.checkHome)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_HONK_Dialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var isUpdating = false

        checkAll.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener
            isUpdating = true
            if (isChecked) {
                listOf(checkWork, checkPet, checkSchool, checkHome).forEach {
                    it.isChecked = false
                }
            }
            isUpdating = false
        }

        val themeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (isUpdating) return@OnCheckedChangeListener
            isUpdating = true
            if (checkWork.isChecked || checkPet.isChecked || checkSchool.isChecked || checkHome.isChecked) {
                checkAll.isChecked = false
            }
            isUpdating = false
        }

        checkWork.setOnCheckedChangeListener(themeListener)
        checkPet.setOnCheckedChangeListener(themeListener)
        checkSchool.setOnCheckedChangeListener(themeListener)
        checkHome.setOnCheckedChangeListener(themeListener)
    }

    // -------- Calendar generation --------

    data class DayInfo(
        val day: String,
        val isCurrentMonth: Boolean
    )

    private fun updateCalendar() {
        val tempCalendar = currentCalendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

        var firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDayOfWeek < 0) firstDayOfWeek += 7

        // month label
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        monthLabel.text = monthFormat.format(currentCalendar.time)

        // previous month days to fill grid
        tempCalendar.add(Calendar.MONTH, -1)
        val prevMonthDaysCount = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        tempCalendar.add(Calendar.MONTH, 1)

        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val dayInfos = mutableListOf<DayInfo>()

        // previous month tail
        if (firstDayOfWeek != 0) {
            for (i in prevMonthDaysCount - firstDayOfWeek + 1..prevMonthDaysCount) {
                dayInfos.add(DayInfo(day = i.toString(), isCurrentMonth = false))
            }
        }

        // current month days
        for (d in 1..daysInMonth) {
            dayInfos.add(DayInfo(day = d.toString(), isCurrentMonth = true))
        }

        calendarGrid.adapter = DaysAdapter(dayInfos)
    }


    // -------- Update visible reminders --------

    private fun updateReminderList() {
        val currentDate = selectedDate ?: return
        val all = TaskRepository.tasks.value ?: emptyList()
        val filtered = all.filter { it.date == currentDate }
        reminderAdapter.setReminders(filtered)
    }

    // -------- Days grid adapter --------

    inner class DaysAdapter(
        private val days: List<DayInfo>
    ) : BaseAdapter() {

        override fun getCount() = days.size
        override fun getItem(position: Int) = days[position]
        override fun getItemId(position: Int) = position.toLong()

        fun getDayInfo(position: Int): DayInfo? =
            days.getOrNull(position)

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_day, parent, false)
            val dayText = view.findViewById<TextView>(R.id.dayText) ?: view as TextView

            val dayInfo = days[position]
            dayText.text = dayInfo.day

            val today = Calendar.getInstance()
            val isToday =
                dayInfo.isCurrentMonth &&
                        currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                        dayInfo.day.toInt() == today.get(Calendar.DAY_OF_MONTH)

            if (isToday) {
                dayText.setBackgroundResource(R.drawable.day_cell_today_bg)
                dayText.alpha = 1.0f
            } else {
                dayText.setBackgroundResource(R.drawable.day_cell_bg)
                // Previous/next month days semi-transparent
                dayText.alpha = if (dayInfo.isCurrentMonth) 1.0f else 0.3f
            }

            return view
        }
    }

    // -------- Reminders list adapter --------

    inner class ReminderAdapter(
        private val onReminderClick: (Reminder) -> Unit,
        private val onDeleteClick: (Reminder) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        private var reminderItems: List<Reminder> = emptyList()

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
            val categoryPart = if (r.category.isNotBlank()) " (${r.category})" else ""
            holder.text.text = "${r.time} – ${r.text}$categoryPart"

            val color = when (r.category.uppercase(Locale.getDefault())) {
                "WORK" -> 0xFF03A9F4.toInt()
                "SCHOOL" -> 0xFF4CAF50.toInt()
                "PETS", "PET" -> 0xFFF44336.toInt()
                "HOME" -> 0xFF9C27B0.toInt()
                else -> 0xFF888888.toInt()
            }
            holder.text.setTextColor(color)
        }
    }
}
