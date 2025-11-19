package com.example.honk.ui.notes

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.local.LocalReminderRepository
import com.example.honk.model.Reminder
import com.example.honk.ui.categories.CategoryViewModel
import com.example.honk.ui.dialogs.TaskDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton

class NotesFragment : Fragment() {

    private lateinit var notesRecycler: RecyclerView
    private lateinit var adapter: NotesAdapter
    private lateinit var categoryViewModel: CategoryViewModel

    private var activeDateFilter: String? = null
    private var activeCategoryFilter: String? = null
    private var activePriorityFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        categoryViewModel =
            ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        notesRecycler = view.findViewById(R.id.notesRecycler)
        notesRecycler.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotesAdapter(mutableListOf())
        notesRecycler.adapter = adapter

        // Observe global reminder list
        LocalReminderRepository.reminders.observe(viewLifecycleOwner, Observer {
            applyFilters()
        })

        // Add note (using TaskDialog)
        view.findViewById<FloatingActionButton>(R.id.fab_add_note)
            .setOnClickListener {
                TaskDialog.show(
                    fragment = this,
                    onSave = { task -> LocalReminderRepository.add(task) }
                )
            }

        view.findViewById<ImageButton>(R.id.filterButton)
            .setOnClickListener { showFilterDialog() }

        return view
    }

    // FILTERING
    private fun applyFilters() {
        val all = LocalReminderRepository.reminders.value ?: emptyList()

        val filtered = all.filter { note ->
            val matchDate = activeDateFilter?.let { note.date == it } ?: true
            val matchCategory = activeCategoryFilter?.let {
                note.category.equals(it, ignoreCase = true)
            } ?: true
            val matchPriority = activePriorityFilter?.let {
                note.priority.equals(it, ignoreCase = true)
            } ?: true

            matchDate && matchCategory && matchPriority
        }

        adapter.updateData(filtered)
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_notes, null)

        val dateSpinner = dialogView.findViewById<Spinner>(R.id.filterDateSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.filterCategorySpinner)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.filterPrioritySpinner)
        val applyButton = dialogView.findViewById<Button>(R.id.applyFilterButton)

        val list = LocalReminderRepository.reminders.value ?: emptyList()

        val dates = listOf("All") + list.map { it.date }.distinct()
        val categories = listOf("All") + list.map { it.category }.filter { it.isNotBlank() }.distinct()
        val priorities = listOf("All", "High", "Medium", "Low")

        dateSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, dates)
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, categories)
        prioritySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, priorities)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        applyButton.setOnClickListener {
            activeDateFilter = dateSpinner.selectedItem.toString().takeIf { it != "All" }
            activeCategoryFilter = categorySpinner.selectedItem.toString().takeIf { it != "All" }
            activePriorityFilter = prioritySpinner.selectedItem.toString().takeIf { it != "All" }

            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    // ADAPTER
    inner class NotesAdapter(private val displayed: MutableList<Reminder>) :
        RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

        inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.noteText)
            val checkBox: CheckBox = view.findViewById(R.id.noteCheckBox)
            val edit: ImageButton = view.findViewById(R.id.editNoteButton)
            val delete: ImageButton = view.findViewById(R.id.deleteNoteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(v)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = displayed[position]

            holder.text.text = "${note.date}: ${note.text}"
            holder.checkBox.isChecked = note.isDone

            holder.checkBox.setOnCheckedChangeListener { _, checked ->
                note.isDone = checked
                LocalReminderRepository.update(note)
            }

            holder.edit.setOnClickListener {
                TaskDialog.show(
                    fragment = this@NotesFragment,
                    existing = note,
                    onSave = { LocalReminderRepository.update(it) },
                    onDelete = { LocalReminderRepository.delete(note) }
                )
            }

            holder.delete.setOnClickListener {
                LocalReminderRepository.delete(note)
            }
        }

        override fun getItemCount() = displayed.size

        fun updateData(newData: List<Reminder>) {
            displayed.clear()
            displayed.addAll(newData)
            notifyDataSetChanged()
        }
    }
}
