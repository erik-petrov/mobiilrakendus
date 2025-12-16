package com.example.honk.ui.dialogs

import android.app.DatePickerDialog
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import android.widget.*
import androidx.lifecycle.ViewModelProvider
import com.example.honk.R
import com.example.honk.model.Reminder
import com.example.honk.ui.categories.CategoryViewModel
import java.util.*
import android.text.InputFilter
import android.text.InputType
import com.example.honk.notifications.ReminderNotificationScheduler
import com.example.honk.model.ReminderOffset


object TaskDialog {

    fun show(
        fragment: Fragment,
        existing: Reminder? = null,
        presetDate: String? = null,
        presetCategory: String? = null,
        onSave: (Reminder) -> Unit,
        onDelete: (() -> Unit)? = null
    ) {
        val ctx = fragment.requireContext()
        val view = fragment.layoutInflater.inflate(R.layout.dialog_add_task, null)

        val title = view.findViewById<TextView>(R.id.dialogTitle)
        val text = view.findViewById<EditText>(R.id.noteText)
        val date = view.findViewById<EditText>(R.id.noteDate)
        val time = view.findViewById<EditText>(R.id.noteTime)
        val remindBeforeSpinner = view.findViewById<Spinner>(R.id.spinner_remind_before)
        val categorySpinner = view.findViewById<Spinner>(R.id.noteCategorySpinner)
        val saveButton = view.findViewById<Button>(R.id.addButton)
        val prioritySpinner = view.findViewById<Spinner>(R.id.notePrioritySpinner)

        // Priority dropdown setup
        val priorities = listOf("Select priority","High", "Medium", "Low")

        val priorityAdapter = ArrayAdapter(
            ctx,
            android.R.layout.simple_spinner_dropdown_item,
            priorities
        )

        prioritySpinner.adapter = priorityAdapter


        text.filters = arrayOf(InputFilter.LengthFilter(90))
        time.filters = arrayOf(InputFilter.LengthFilter(5))
        time.inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME

        val vm = ViewModelProvider(fragment.requireActivity())
            .get(CategoryViewModel::class.java)

        val repo = ViewModelProvider(fragment.requireActivity())
            .get(ReminderViewModel::class.java)
        // load categories dynamically and update spinner when loaded
        vm.categories.observe(fragment.viewLifecycleOwner) { list ->
            val categories = listOf("No category") + list.map { it.name }

            val adapter = ArrayAdapter(
                ctx,
                android.R.layout.simple_spinner_dropdown_item,
                categories
            )
            categorySpinner.adapter = adapter

            // choose correct selected category
            val selectedName = when {
                existing != null -> existing.category
                presetCategory != null -> presetCategory
                else -> ""
            }

            val index = categories.indexOf(selectedName).takeIf { it >= 0 } ?: 0
            categorySpinner.setSelection(index)
        }

        // Date picker
        date.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(
                ctx, { _, y, m, d ->
                    date.setText("%02d.%02d.%d".format(d, m + 1, y))
                },
                c.get(Calendar.YEAR),
                c.get(Calendar.MONTH),
                c.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // If editing an existing task
        if (existing != null) {
            title.text = "Edit Task"
            saveButton.text = "Save"

            text.setText(existing.text)
            date.setText(existing.date)
            time.setText(existing.time)
            val priorityIndex = when (val p = existing.priority) {
                "", null -> 0
                else -> priorities.indexOf(p).takeIf { it > 0 } ?: 0
            }
            prioritySpinner.setSelection(priorityIndex)

            val pos = when (existing.reminderOffset) {
                ReminderOffset.NONE -> 0
                ReminderOffset.ONE_HOUR -> 1
                ReminderOffset.TWO_HOURS -> 2
                ReminderOffset.ONE_DAY -> 3
                ReminderOffset.TWO_DAYS -> 4
                ReminderOffset.ONE_WEEK -> 5
            }
            remindBeforeSpinner.setSelection(pos)
        }

        // If calendar provides a preset date
        presetDate?.let { date.setText(it) }

        val dialog = AlertDialog.Builder(ctx, R.style.Theme_HONK_Dialog)
            .setView(view)
            .apply {
                if (onDelete != null)
                    setNegativeButton("Delete") { _, _ -> onDelete() }
            }
            .create()

        saveButton.setOnClickListener {
            val category = categorySpinner.selectedItem.toString().let {
                if (it == "No category") "" else it
            }

            val result = existing ?: Reminder(
                date = date.text.toString()
            )

            result.text = text.text.toString()
            result.date = date.text.toString()
            result.time = time.text.toString()
            val selectedPriority = prioritySpinner.selectedItem.toString()

            result.priority = if (selectedPriority == "Select priority") {
                ""
            } else {
                selectedPriority
            }
            result.category = category

            result.reminderOffset = when (remindBeforeSpinner.selectedItemPosition) {
                1 -> ReminderOffset.ONE_HOUR
                2 -> ReminderOffset.TWO_HOURS
                3 -> ReminderOffset.ONE_DAY
                4 -> ReminderOffset.TWO_DAYS
                5 -> ReminderOffset.ONE_WEEK
                else -> ReminderOffset.NONE
            }

            if (existing == null) {
                result.notificationId = System.currentTimeMillis()
            }

            onSave(result)

            repo.add(result)

            ReminderNotificationScheduler.schedule(fragment.requireContext(), result)

            dialog.dismiss()
        }

        dialog.show()

        // Make dialog wider + rounded
        dialog.window?.setLayout(
            (fragment.resources.displayMetrics.widthPixels * 0.85).toInt(),
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

//    private fun scheduleReminderIfNeeded(
//        fragment: Fragment,
//        reminder: Reminder,
//        spinnerPosition: Int
//    ) {
//        val ctx = fragment.requireContext()
//
//        val dateStr = reminder.date
//        val timeStr = reminder.time
//
//        if (dateStr.isBlank() || timeStr.isBlank()) {
//            return
//        }
//
//        val taskTimeMillis = parseDateTimeToMillis(dateStr, timeStr) ?: return
//
//        val offsetMillis = getOffsetMillis(spinnerPosition)
//        if (offsetMillis <= 0L) return  // "No reminder"
//
//        val triggerAt = taskTimeMillis - offsetMillis
//        if (triggerAt <= System.currentTimeMillis()) {
//            return
//        }
//
//        // A temporary way to make the reminder ID before the normal database
//        val taskId = "${reminder.date}_${reminder.time}_${reminder.text}"
//
//        val message = buildString {
//            append("Task: ${reminder.text}")
//            if (reminder.time.isNotBlank()) {
//                append(" at ${reminder.time}")
//            }
//            if (reminder.date.isNotBlank()) {
//                append(" on ${reminder.date}")
//            }
//        }
//
//        TaskAlarmScheduler.scheduleTaskReminder(
//            context = ctx,
//            taskId = taskId,
//            title = "Upcoming task",
//            message = message,
//            triggerAtMillis = triggerAt
//        )
//    }

//    private fun parseDateTimeToMillis(dateStr: String, timeStr: String): Long? {
//        return try {
//            val format = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
//            val date = format.parse("$dateStr $timeStr")
//            date?.time
//        } catch (e: Exception) {
//            e.printStackTrace()
//            null
//        }
//    }

//    private fun getOffsetMillis(position: Int): Long {
//        return when (position) {
//            0 -> 0L                                  // No reminder
//            1 -> 1L * 60 * 60 * 1000                 // 1 hour
//            2 -> 2L * 60 * 60 * 1000                 // 2 hours
//            3 -> 1L * 24 * 60 * 60 * 1000            // 1 day
//            4 -> 2L * 24 * 60 * 60 * 1000            // 2 days
//            5 -> 7L * 24 * 60 * 60 * 1000            // 1 week
//            else -> 0L
//        }
//    }
}
