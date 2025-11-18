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
        val categorySpinner = view.findViewById<Spinner>(R.id.noteCategorySpinner)
        val priority = view.findViewById<EditText>(R.id.notePriority)
        val saveButton = view.findViewById<Button>(R.id.addButton)

        val vm = ViewModelProvider(fragment.requireActivity())
            .get(CategoryViewModel::class.java)

        // 🔥 load categories dynamically and update spinner when loaded
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
            priority.setText(existing.priority)
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
            result.priority = priority.text.toString()
            result.category = category

            onSave(result)
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
}
