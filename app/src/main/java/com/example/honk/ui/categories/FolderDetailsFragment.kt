package com.example.honk.ui.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FolderDetailsFragment : Fragment() {

    private lateinit var categoryTitle: TextView
    private lateinit var reminderRecycler: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter

    private var currentCategoryName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_folder_details, container, false)

        categoryTitle = view.findViewById(R.id.categoryTitle)
        reminderRecycler = view.findViewById(R.id.reminderRecycler)
        val backButton = view.findViewById<ImageButton>(R.id.backButton)
        val editButton = view.findViewById<ImageButton>(R.id.editCategory)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteCategory)
        val fabAddReminder = view.findViewById<FloatingActionButton>(R.id.fab_add_reminder)

        // Get category name from arguments
        currentCategoryName = arguments?.getString("category_name") ?: "Unknown Category"
        categoryTitle.text = currentCategoryName

        // Setup recycler view
        reminderRecycler.layoutManager = LinearLayoutManager(requireContext())
        reminderAdapter = ReminderAdapter(mutableListOf())
        reminderRecycler.adapter = reminderAdapter

        backButton.setOnClickListener {
            findNavController().navigateUp()
        }
        editButton.setOnClickListener {
            Toast.makeText(requireContext(), "Edit clicked!", Toast.LENGTH_SHORT).show()
            showEditCategoryDialog()
        }
        deleteButton.setOnClickListener {
            Toast.makeText(requireContext(), "Delete clicked!", Toast.LENGTH_SHORT).show()
            showDeleteCategoryDialog()
        }

        fabAddReminder.setOnClickListener {
            Toast.makeText(requireContext(), "Add reminder clicked!", Toast.LENGTH_SHORT).show()
            showAddReminderDialog()
        }

        return view
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val noteText = dialogView.findViewById<android.widget.EditText>(R.id.noteText)
        val noteDate = dialogView.findViewById<android.widget.EditText>(R.id.noteDate)
        val notePriority = dialogView.findViewById<android.widget.EditText>(R.id.notePriority)
        val addButton = dialogView.findViewById<android.widget.Button>(R.id.addNoteButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        addButton.setOnClickListener {
            val text = noteText.text.toString().ifBlank { "(empty)" }
            val date = noteDate.text.toString().ifBlank { "—" }
            val priority = notePriority.text.toString().ifBlank { "Medium" }

            val reminder = Reminder(date, "", text, currentCategoryName, false, priority)
            reminderAdapter.addReminder(reminder)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<android.widget.EditText>(R.id.categoryName)
        val addButton = dialogView.findViewById<android.widget.Button>(R.id.addCategoryButton)

        // Pre-fill with current name
        nameField.setText(currentCategoryName)
        addButton.text = "Save"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        addButton.setOnClickListener {
            val newName = nameField.text.toString().ifBlank { "Untitled" }
            currentCategoryName = newName
            categoryTitle.text = newName

            Toast.makeText(requireContext(), "Category renamed to: $newName", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showDeleteCategoryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete category?")
            .setMessage("This will remove \"$currentCategoryName\" and all its reminders.")
            .setPositiveButton("Delete") { _, _ ->
                // Show confirmation message
                Toast.makeText(
                    requireContext(),
                    "Category \"$currentCategoryName\" deleted",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    inner class ReminderAdapter(private val reminders: MutableList<Reminder>) :
        RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

        inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val checkBox: android.widget.CheckBox = view.findViewById(R.id.noteCheckBox)
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
                        reminders.removeAt(position)
                        notifyItemRemoved(position)
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
            val noteText = dialogView.findViewById<android.widget.EditText>(R.id.noteText)
            val noteDate = dialogView.findViewById<android.widget.EditText>(R.id.noteDate)
            val notePriority = dialogView.findViewById<android.widget.EditText>(R.id.notePriority)
            val addButton = dialogView.findViewById<android.widget.Button>(R.id.addNoteButton)

            noteText.setText(reminder.text)
            noteDate.setText(reminder.date)
            notePriority.setText(reminder.priority ?: "")
            addButton.text = "Save Changes"

            val dialog = AlertDialog.Builder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
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