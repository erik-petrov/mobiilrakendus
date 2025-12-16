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
import android.app.TimePickerDialog


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

        time.isFocusable = false
        time.isClickable = true

        time.setOnClickListener {
            val now = Calendar.getInstance()

            val currentText = time.text.toString().trim()
            val (initH, initM) = parseTimeOrDefault(currentText, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

            TimePickerDialog(ctx, { _, hourOfDay, minute ->
                time.setText(String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute))
            }, initH, initM, true).show()
        }

        val remindBeforePicker = view.findViewById<TextView>(R.id.spinner_remind_before)

        val offsetOptions = listOf(
            ReminderOffset.ONE_HOUR,
            ReminderOffset.TWO_HOURS,
            ReminderOffset.ONE_DAY,
            ReminderOffset.TWO_DAYS,
            ReminderOffset.ONE_WEEK
        )

        val offsetLabels = arrayOf("1 hour", "2 hours", "1 day", "2 days", "1 week")

        val selectedOffsets = mutableSetOf<ReminderOffset>()

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
//        time.filters = arrayOf(InputFilter.LengthFilter(5))
//        time.inputType = InputType.TYPE_CLASS_DATETIME or InputType.TYPE_DATETIME_VARIATION_TIME
        time.inputType = InputType.TYPE_NULL

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

        remindBeforePicker.setOnClickListener {
            val checked = BooleanArray(offsetOptions.size) { i ->
                selectedOffsets.contains(offsetOptions[i])
            }

            AlertDialog.Builder(ctx, R.style.Theme_HONK_Dialog)
                .setTitle("Remind before")
                .setMultiChoiceItems(offsetLabels, checked) { _, which, isChecked ->
                    val off = offsetOptions[which]
                    if (isChecked) selectedOffsets.add(off) else selectedOffsets.remove(off)
                }
                .setPositiveButton("OK") { _, _ ->
                    remindBeforePicker.text = formatOffsets(selectedOffsets)
                }
                .setNegativeButton("Cancel", null)
                .show()
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

            if (existing?.reminderOffsets?.isNotEmpty() == true) {
                existing.reminderOffsets.forEach { name ->
                    runCatching { ReminderOffset.valueOf(name) }.getOrNull()?.let { off ->
                        if (off.minutes > 0) selectedOffsets.add(off)
                    }
                }
            } else if (existing != null && existing.reminderOffset.minutes > 0) {
                selectedOffsets.add(existing.reminderOffset)
            }

            remindBeforePicker.text = formatOffsets(selectedOffsets)
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

            result.reminderOffsets = selectedOffsets
                .sortedBy { it.minutes }
                .map { it.name }

            result.reminderOffset = selectedOffsets.minByOrNull { it.minutes } ?: ReminderOffset.NONE

            if (existing == null) {
                result.notificationId = System.currentTimeMillis()
            }

            onSave(result)

            repo.add(result)

            ReminderNotificationScheduler.schedule(fragment.requireContext().applicationContext, result)

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

    private fun parseTimeOrDefault(text: String, defH: Int, defM: Int): Pair<Int, Int> {
        return try {
            val parts = text.split(":")
            if (parts.size != 2) return defH to defM
            val h = parts[0].toInt()
            val m = parts[1].toInt()
            if (h !in 0..23 || m !in 0..59) defH to defM else h to m
        } catch (_: Exception) {
            defH to defM
        }
    }

    private fun formatOffsets(set: Set<ReminderOffset>): String {
        if (set.isEmpty()) return "No reminder"
        return set.sortedBy { it.minutes }.joinToString(", ") { off ->
            when (off) {
                ReminderOffset.ONE_HOUR -> "1 hour"
                ReminderOffset.TWO_HOURS -> "2 hours"
                ReminderOffset.ONE_DAY -> "1 day"
                ReminderOffset.TWO_DAYS -> "2 days"
                ReminderOffset.ONE_WEEK -> "1 week"
                else -> ""
            }
        }
    }

}
