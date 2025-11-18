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
import com.example.honk.data.TaskRepository
import com.example.honk.model.Reminder
import com.example.honk.ui.dialogs.TaskDialog
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

        val category = viewModel.categories.value?.getOrNull(categoryIndex)
        val categoryName = category?.name ?: "(unknown)"

        categoryTitle = view.findViewById(R.id.categoryTitle)
        categoryTitle.text = categoryName

        reminderRecycler = view.findViewById(R.id.reminderRecycler)
        reminderRecycler.layoutManager = LinearLayoutManager(requireContext())

        reminderAdapter = ReminderAdapter(mutableListOf())
        reminderRecycler.adapter = reminderAdapter

        // Keep list in this folder synced with global tasks
        TaskRepository.tasks.observe(viewLifecycleOwner) {
            refreshReminders()
        }

        view.findViewById<ImageButton>(R.id.backButton).setOnClickListener {
            findNavController().navigateUp()
        }

        view.findViewById<ImageButton>(R.id.editCategory).setOnClickListener {
            showEditCategoryDialog()
        }

        view.findViewById<ImageButton>(R.id.deleteCategory).setOnClickListener {
            showDeleteCategoryDialog()
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_reminder)
            .setOnClickListener {
                // unified TaskDialog, pre-filled category for this folder
                val catName = viewModel.categories.value?.getOrNull(categoryIndex)?.name
                TaskDialog.show(
                    fragment = this,
                    presetCategory = catName,
                    onSave = { task -> TaskRepository.addTask(task) }
                )
            }

        // initial load
        refreshReminders()

        return view
    }

    private fun refreshReminders() {
        val categoryName = viewModel.categories.value?.getOrNull(categoryIndex)?.name ?: return
        val all = TaskRepository.tasks.value ?: mutableListOf()
        val filtered = all.filter { it.category == categoryName }
        reminderAdapter.updateData(filtered)
    }

    private fun showEditCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<EditText>(R.id.categoryName)
        val colorPreview = dialogView.findViewById<View>(R.id.colorPreview)
        val pickColorButton = dialogView.findViewById<Button>(R.id.pickColorButton)
        val saveBtn = dialogView.findViewById<Button>(R.id.addCategoryButton)

        val cat = viewModel.categories.value?.getOrNull(categoryIndex)
        if (cat != null) {
            nameField.setText(cat.name)
            colorPreview.setBackgroundColor(cat.color)
        }

        saveBtn.text = "Save"

        var selectedColor = cat?.color ?: requireContext().getColor(R.color.category_blue)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        pickColorButton.setOnClickListener {
            showColorPickerDialog { picked ->
                selectedColor = picked
                colorPreview.setBackgroundColor(picked)
            }
        }

        saveBtn.setOnClickListener {
            val newName = nameField.text.toString().ifBlank { "Untitled" }
            viewModel.updateCategory(categoryIndex, newName, selectedColor)
            categoryTitle.text = newName
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showColorPickerDialog(onColorPicked: (Int) -> Unit) {
        val pickerView = layoutInflater.inflate(R.layout.dialog_color_picker, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(pickerView)
            .setCancelable(true)
            .create()

        fun View.pick(colorRes: Int) {
            setOnClickListener {
                val color = requireContext().getColor(colorRes)
                onColorPicked(color)
                dialog.dismiss()
            }
        }

        pickerView.findViewById<View>(R.id.colorRed)
            .pick(R.color.category_red)

        pickerView.findViewById<View>(R.id.colorGreen)
            .pick(R.color.category_green)

        pickerView.findViewById<View>(R.id.colorBlue)
            .pick(R.color.category_blue)

        pickerView.findViewById<View>(R.id.colorYellow)
            .pick(R.color.category_yellow)

        dialog.show()
    }

    private fun showDeleteCategoryDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete category?")
            .setMessage("This will delete ${categoryTitle.text} and all its reminders.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(categoryIndex)
                findNavController().navigateUp()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // -------------------------------------------------------------------

    inner class ReminderAdapter(private val reminders: MutableList<Reminder>) :
        RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

        inner class ReminderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val checkBox: CheckBox = view.findViewById(R.id.noteCheckBox)
            val text: TextView = view.findViewById(R.id.noteText)
            val edit: ImageButton = view.findViewById(R.id.editNoteButton)
            val delete: ImageButton = view.findViewById(R.id.deleteNoteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_note, parent, false)
            return ReminderViewHolder(v)
        }

        override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
            val r = reminders[position]

            holder.text.text = "${r.date}: ${r.text}"
            holder.checkBox.isChecked = r.isDone

            holder.checkBox.setOnCheckedChangeListener { _, checked ->
                r.isDone = checked
                TaskRepository.updateTask(r)
            }

            // Edit uses the same TaskDialog
            holder.edit.setOnClickListener {
                TaskDialog.show(
                    fragment = this@FolderDetailsFragment,
                    existing = r,
                    onSave = { updated -> TaskRepository.updateTask(updated) },
                    onDelete = { TaskRepository.deleteTask(r) }
                )
            }

            holder.delete.setOnClickListener {
                TaskRepository.deleteTask(r)
            }
        }

        override fun getItemCount() = reminders.size

        fun updateData(newList: List<Reminder>) {
            reminders.clear()
            reminders.addAll(newList)
            notifyDataSetChanged()
        }
    }
}
