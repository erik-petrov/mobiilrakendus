package com.example.honk.ui.categories

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.honk.model.Reminder
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController

data class Category(
    var name: String,
    var color: Int,
    var reminders: MutableList<Reminder> = mutableListOf()
)

class CategoriesFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private val categories = mutableListOf(
        Category("SCHOOL", R.color.category_red),
        Category("PETS", R.color.category_green),
        Category("HOLIDAY", R.color.category_yellow),
        Category("WORK", R.color.category_blue)
    )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        recycler = view.findViewById(R.id.categoriesRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)
        adapter = CategoryAdapter(categories)
        recycler.adapter = adapter

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fab_add_category)
        fabAdd.setOnClickListener { showAddCategoryDialog() }

        return view
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<EditText>(R.id.categoryName)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addCategoryButton)

        val colorOptions = mapOf(
            "Red" to R.color.category_red,
            "Green" to R.color.category_green,
            "Blue" to R.color.category_blue,
            "Yellow" to R.color.category_yellow
        )

        colorSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            colorOptions.keys.toList()
        )

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        addButton.setOnClickListener {
            val name = nameField.text.toString().ifBlank { "Untitled" }
            val colorName = colorSpinner.selectedItem.toString()
            val colorRes = colorOptions[colorName] ?: R.color.category_blue

            categories.add(Category(name, colorRes))
            adapter.notifyItemInserted(categories.size - 1)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showAddReminderDialog(category: Category, adapter: ReminderAdapter) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val noteText = dialogView.findViewById<EditText>(R.id.noteText)
        val noteDate = dialogView.findViewById<EditText>(R.id.noteDate)
        val notePriority = dialogView.findViewById<EditText>(R.id.notePriority)
        val addButton = dialogView.findViewById<Button>(R.id.addNoteButton)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        addButton.setOnClickListener {
            val text = noteText.text.toString().ifBlank { "(empty)" }
            val date = noteDate.text.toString().ifBlank { "—" }
            val priority = notePriority.text.toString().ifBlank { "Medium" }

            val reminder = Reminder(date, "", text, category.name, false, priority)
            category.reminders.add(reminder)
            adapter.notifyItemInserted(category.reminders.size - 1)

            dialog.dismiss()
        }

        dialog.show()
    }


    inner class CategoryAdapter(private val data: List<Category>) :
        RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

        inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val layout: LinearLayout = view.findViewById(R.id.categoryCard)
            val name: TextView = view.findViewById(R.id.categoryNameText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_category, parent, false)
            return CategoryViewHolder(view)
        }

        override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
            val category = data[position]
            holder.name.text = category.name
            holder.layout.setBackgroundResource(category.color)

            holder.itemView.setOnClickListener {
                showCategoryDetails(category)
            }
        }

        override fun getItemCount() = data.size
    }

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

            // Edit button
            holder.editButton.setOnClickListener {
                showEditReminderDialog(reminder, position)
            }

            // Delete button
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
    }

    private fun showEditReminderDialog(reminder: Reminder, position: Int) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_note, null)
        val noteText = dialogView.findViewById<EditText>(R.id.noteText)
        val noteDate = dialogView.findViewById<EditText>(R.id.noteDate)
        val notePriority = dialogView.findViewById<EditText>(R.id.notePriority)
        val addButton = dialogView.findViewById<Button>(R.id.addNoteButton)

        // Prefill fields
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
            adapter.notifyItemChanged(position)
            dialog.dismiss()
        }

        dialog.show()
    }

    /*private fun showCategoryDetails(category: Category) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_category_details, null)
        val title = dialogView.findViewById<TextView>(R.id.categoryTitle)
        val closeButton = dialogView.findViewById<ImageButton>(R.id.closeButton)
        val deleteButton = dialogView.findViewById<ImageButton>(R.id.deleteCategory)
        val editButton = dialogView.findViewById<ImageButton>(R.id.editCategory)
        val fabAddReminder = dialogView.findViewById<FloatingActionButton>(R.id.fab_add_reminder)
        val recycler = dialogView.findViewById<RecyclerView>(R.id.reminderRecycler)

        title.text = category.name

        // Setup the reminders list
        recycler.layoutManager = LinearLayoutManager(requireContext())
        val reminderAdapter = ReminderAdapter(category.reminders)
        recycler.adapter = reminderAdapter

        // Dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Close button
        closeButton.setOnClickListener { dialog.dismiss() }

        // Delete button
        deleteButton.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete category?")
                .setMessage("This will remove \"${category.name}\" and its reminders.")
                .setPositiveButton("Delete") { _, _ ->
                    val idx = categories.indexOf(category)
                    if (idx >= 0) {
                        categories.removeAt(idx)
                        adapter.notifyItemRemoved(idx)
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Edit button
        editButton.setOnClickListener {
            showEditCategoryDialog(category) {
                title.text = category.name
                val idx = categories.indexOf(category)
                if (idx >= 0) adapter.notifyItemChanged(idx)
            }
        }

        fabAddReminder.setOnClickListener {
            showAddReminderDialog(category, reminderAdapter)
        }

        dialog.show()
    }   */

    private fun showCategoryDetails(category: Category) {
        val index = categories.indexOf(category)
        val bundle = Bundle()
        bundle.putString("category_name", category.name)
        bundle.putInt("category_index", index)

        findNavController().navigate(R.id.folderDetailsFragment, bundle)
    }

    private fun showEditCategoryDialog(category: Category, onUpdated: () -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<EditText>(R.id.categoryName)
        val colorSpinner = dialogView.findViewById<Spinner>(R.id.colorSpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addCategoryButton)

        // same map you used in add
        val colorOptions = mapOf(
            "Red" to R.color.category_red,
            "Green" to R.color.category_green,
            "Blue" to R.color.category_blue,
            "Yellow" to R.color.category_yellow
        )

        colorSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            colorOptions.keys.toList()
        )

        // Pre-fill
        nameField.setText(category.name)
        // select current color
        val currentKey = colorOptions.entries.firstOrNull { it.value == category.color }?.key
        val indexToSelect = colorOptions.keys.indexOf(currentKey ?: "Blue")
        if (indexToSelect >= 0) colorSpinner.setSelection(indexToSelect)

        addButton.text = "Save"

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        addButton.setOnClickListener {
            val newName = nameField.text.toString().ifBlank { "Untitled" }
            val selectedColorName = colorSpinner.selectedItem.toString()
            val newColorRes = colorOptions[selectedColorName] ?: R.color.category_blue

            category.name = newName
            category.color = newColorRes

            onUpdated()
            dialog.dismiss()
        }

        dialog.show()
    }
}
