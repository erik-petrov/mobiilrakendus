package com.example.honk.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FolderDetailsFragment : Fragment() {

    private lateinit var viewModel: CategoryViewModel
    private lateinit var categoryTitle: TextView
    private lateinit var reminderRecycler: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter
    private var categoryIndex: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_folder_details, container, false)

        viewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryIndex = arguments?.getInt("category_index") ?: -1

        categoryTitle = view.findViewById(R.id.categoryTitle)
        reminderRecycler = view.findViewById(R.id.reminderRecycler)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val editButton = view.findViewById<ImageButton>(R.id.editCategory)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteCategory)
        val fabAddReminder = view.findViewById<FloatingActionButton>(R.id.fab_add_reminder)

        val category = viewModel.categories.value?.getOrNull(categoryIndex)
        categoryTitle.text = category?.name ?: "Unknown Category"

        // Recycler setup
        reminderRecycler.layoutManager = LinearLayoutManager(requireContext())
        reminderAdapter = ReminderAdapter(category?.reminders ?: mutableListOf())
        reminderRecycler.adapter = reminderAdapter

        backButton.setOnClickListener { findNavController().navigateUp() }
        editButton.setOnClickListener { showEditCategoryDialog() }
        deleteButton.setOnClickListener { showDeleteCategoryDialog() }
        fabAddReminder.setOnClickListener { showAddReminderDialog() }

        return view
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val noteText = dialogView.findViewById<EditText>(R.id.noteText)
        val noteDate = dialogView.findViewById<EditText>(R.id.noteDate)
        val notePriority = dialogView.findViewById<EditText>(R.id.notePriority)
        val addButton = dialogView.findViewById<Button>(R.id.addNoteButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            val text = noteText.text.toString().ifBlank { "(empty)" }
            val date = noteDate.text.toString().ifBlank { "—" }
            val priority = notePriority.text.toString().ifBlank { "Medium" }

            val reminder = Reminder(date, "", text, categoryTitle.text.toString(), false, priority)

            viewModel.addReminderToCategory(categoryIndex, reminder)

            reminderAdapter.notifyItemInserted(reminderAdapter.itemCount)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<EditText>(R.id.categoryName)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addCategoryButton)

        val currentCategory = viewModel.categories.value?.getOrNull(categoryIndex)
        nameField.setText(currentCategory?.name)
        addButton.text = "Save"

        // same color options as in CategoriesFragment
        val colorOptions = mapOf(
            "Red" to R.color.category_red,
            "Green" to R.color.category_green,
            "Blue" to R.color.category_blue,
            "Yellow" to R.color.category_yellow
        )

        val colorNames = colorOptions.keys.toList()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, colorNames)
        colorSpinner.adapter = adapter

        // preselect the current color
        val currentColorName = colorOptions.entries.firstOrNull { it.value == currentCategory?.color }?.key
        val currentIndex = colorNames.indexOf(currentColorName)
        if (currentIndex >= 0) colorSpinner.setSelection(currentIndex)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        addButton.setOnClickListener {
            val newName = nameField.text.toString().ifBlank { "Untitled" }
            val selectedColorName = colorSpinner.selectedItem.toString()
            val selectedColorRes = colorOptions[selectedColorName] ?: R.color.category_blue

            // Update both name and color in ViewModel
            viewModel.updateCategory(categoryIndex, newName, selectedColorRes)

            // Update UI immediately
            categoryTitle.text = newName
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun showDeleteCategoryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete category?")
            .setMessage("This will remove ${categoryTitle.text} and all its reminders.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(categoryIndex)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ----------------------------
    // ReminderAdapter
    // ----------------------------
    inner class ReminderAdapter(private val reminders: MutableList<Reminder>) :
        RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

        inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val checkBox: CheckBox = view.findViewById(R.id.noteCheckBox)
            val text: TextView = view.findViewById(R.id.noteText)
            val editButton: ImageButton = view.findViewById(R.id.editNoteButton)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteNoteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return ReminderViewHolder(v)
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
            val reminder = reminders[position]
            holder.text.text = "${reminder.date}: ${reminder.text}"
            holder.checkBox.isChecked = reminder.isDone

            holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
                reminder.isDone = isChecked
            }

            holder.editButton.setOnClickListener {
                showEditReminderDialog(reminder, position)
            }

            holder.deleteButton.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete note?")
                    .setMessage("Remove this note from ${reminder.category}?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (position in reminders.indices) {
                            reminders.removeAt(position)
                            notifyItemRemoved(position)
                            // notify ViewModel so observers react
                            viewModel.categories.value = viewModel.categories.value
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

        }

        override fun getItemCount() = reminders.size

        fun addReminder(reminder: Reminder) {
            reminders.add(reminder)
            notifyItemInserted(reminders.size - 1)
        }

        private fun showEditReminderDialog(reminder: Reminder, position: Int) {
            val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
            val noteText = dialogView.findViewById<EditText>(R.id.noteText)
            val noteDate = dialogView.findViewById<EditText>(R.id.noteDate)
            val notePriority = dialogView.findViewById<EditText>(R.id.notePriority)
            val addButton = dialogView.findViewById<Button>(R.id.addNoteButton)

            noteText.setText(reminder.text)
            noteDate.setText(reminder.date)
            notePriority.setText(reminder.priority ?: "")
            addButton.text = "Save Changes"

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .create()

            addButton.setOnClickListener {
                reminder.text = noteText.text.toString()
                reminder.date = noteDate.text.toString()
                reminder.priority = notePriority.text.toString()
                notifyItemChanged(position)
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}
