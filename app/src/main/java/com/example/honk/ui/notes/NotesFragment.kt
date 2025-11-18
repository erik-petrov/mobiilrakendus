package com.example.honk.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import android.widget.ArrayAdapter
import com.example.honk.repository.TaskRepository
import java.text.SimpleDateFormat
import java.util.*

class NotesFragment : Fragment() {

    private lateinit var notesRecycler: RecyclerView
    private lateinit var adapter: NotesAdapter
    private val notes = mutableListOf<Reminder>()  // reuse Reminder data class for now
    private val tr = TaskRepository()

    private var filteredNotes = mutableListOf<Reminder>()
    private var activeDateFilter: String? = null
    private var activeCategoryFilter: String? = null
    private var activePriorityFilter: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        notesRecycler = view.findViewById(R.id.notesRecycler)
        notesRecycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = NotesAdapter(notes)
        notesRecycler.adapter = adapter

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_note)
        fabAdd.setOnClickListener { showAddNoteDialog() }

        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener { showFilterDialog() }

        return view
    }

    private fun addDummyNote() {
        // Temporary test data until you add real input later
        val note = Reminder(
            date = "7 Oct",
            time = "",
            text = "Goose picture",
            category = "HOME"
        )
        notes.add(note)
        adapter.notifyItemInserted(notes.size - 1)
    }

    private fun normalizeDate(input: String): String {
        val possibleFormats = listOf(
            "dd.MM.yyyy", "d.MM.yyyy",
            "dd.MM", "d.MM",
            "dd MMM", "d MMM",
            "yyyy-MM-dd", "yyyy/MM/dd"
        )

        for (format in possibleFormats) {
            try {
                val parsed = java.text.SimpleDateFormat(format, Locale.getDefault()).parse(input)
                if (parsed != null) {
                    // Always reformat to European standard
                    return java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(parsed)
                }
            } catch (_: Exception) {
            }
        }
        return input.trim()
    }

    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter_notes, null)
        val dateSpinner = dialogView.findViewById<Spinner>(R.id.filterDateSpinner)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.filterCategorySpinner)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.filterPrioritySpinner)
        val applyButton = dialogView.findViewById<Button>(R.id.applyFilterButton)

        // example adapter setup – you can replace these with dynamic lists later
        dateSpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("All", "07.10.2025", "08.10.2025", "09.10.2025"))
        categorySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("All", "Home", "Work", "Personal"))
        prioritySpinner.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item,
            listOf("All", "High", "Medium", "Low"))

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        applyButton.setOnClickListener {
            activeDateFilter = dateSpinner.selectedItem.toString().takeIf { it != "All" }
            activeCategoryFilter = categorySpinner.selectedItem.toString().takeIf { it != "All" }
            activePriorityFilter = prioritySpinner.selectedItem.toString().takeIf { it != "All" }

            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun applyFilters() {
        val hasFilter = activeDateFilter != null || activeCategoryFilter != null || activePriorityFilter != null

        if (hasFilter) {
            filteredNotes.clear()
            filteredNotes.addAll(
                notes.filter { note ->
                    val matchDate = activeDateFilter?.let {
                        normalizeDate(note.date) == normalizeDate(it)
                    } ?: true
                    val matchCategory = activeCategoryFilter?.let {
                        note.category.equals(it, true)
                    } ?: true
                    val matchPriority = activePriorityFilter?.let {
                        note.priority.equals(it, ignoreCase = true)
                    } ?: true
                    matchDate && matchCategory && matchPriority
                }
            )
            adapter.updateData(filteredNotes)
        } else {
            adapter.restoreAll()  // restores all notes again
        }
    }

    private fun showAddNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val noteText = dialogView.findViewById<EditText>(R.id.noteText)
        val noteDate = dialogView.findViewById<EditText>(R.id.noteDate)
        val noteCategory = dialogView.findViewById<EditText>(R.id.noteCategory)
        val notePriority = dialogView.findViewById<EditText>(R.id.notePriority)
        val addButton = dialogView.findViewById<Button>(R.id.addNoteButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        addButton.setOnClickListener {
            val text = noteText.text.toString().ifBlank { "(empty)" }
            val date = normalizeDate(noteDate.text.toString())
            val category = noteCategory.text.toString()
            val priority = notePriority.text.toString()

            val note = Reminder(
                date = date,
                time = "",
                text = text,
                category = category.ifBlank { "General" },
                priority = priority.ifBlank { "Medium" }
            )

            notes.add(note)

            // If no filters are active, show all notes again
            if (activeDateFilter == null && activeCategoryFilter == null && activePriorityFilter == null) {
                adapter.restoreAll()
            } else {
                applyFilters() // reapply filters so new note appears if it matches
            }

            dialog.dismiss()

        }

        dialog.show()
    }


    // Simple adapter for displaying notes
    inner class NotesAdapter(private val allNotes: MutableList<Reminder>) :
        RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

        // This list holds what is actually displayed (filtered or full)
        private val displayedData = mutableListOf<Reminder>().apply { addAll(allNotes) }

        inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val checkBox: CheckBox = view.findViewById(R.id.noteCheckBox)
            val text: TextView = view.findViewById(R.id.noteText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return NoteViewHolder(v)
        }

        override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
            val note = displayedData[position]
            holder.text.text = "${note.date}: ${note.text}"
            holder.checkBox.isChecked = false
        }

        override fun getItemCount() = displayedData.size

        // Called when a filter is applied
        fun updateData(newData: List<Reminder>) {
            displayedData.clear()
            displayedData.addAll(newData)
            notifyDataSetChanged()
        }

        // Called when filters are cleared
        fun restoreAll() {
            updateData(allNotes)
        }
    }
}
