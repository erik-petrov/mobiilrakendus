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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CategoriesFragment : Fragment() {

    private lateinit var recycler: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private lateinit var viewModel: CategoryViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_categories, container, false)

        viewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]

        recycler = view.findViewById(R.id.categoriesRecycler)
        recycler.layoutManager = GridLayoutManager(requireContext(), 2)

        adapter = CategoryAdapter(mutableListOf())
        recycler.adapter = adapter

        viewModel.categories.observe(viewLifecycleOwner) { updated ->
            adapter.updateData(updated)
        }

        if (viewModel.categories.value.isNullOrEmpty()) {
            viewModel.categories.value = mutableListOf(
                Category("SCHOOL", R.color.category_red),
                Category("PETS", R.color.category_green),
                Category("HOLIDAY", R.color.category_yellow),
                Category("WORK", R.color.category_blue)
            )
        }

        view.findViewById<FloatingActionButton>(R.id.fab_add_category)
            .setOnClickListener { showAddCategoryDialog() }

        return view
    }

    private fun showAddCategoryDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_category, null)
        val nameField = dialogView.findViewById<EditText>(R.id.categoryName)
        val colorPreview = dialogView.findViewById<View>(R.id.colorPreview)
        val pickColorButton = dialogView.findViewById<Button>(R.id.pickColorButton)
        val addButton = dialogView.findViewById<Button>(R.id.addCategoryButton)

        // default color
        var selectedColor = requireContext().getColor(R.color.category_blue)
        colorPreview.setBackgroundColor(selectedColor)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .create()

        pickColorButton.setOnClickListener {
            showColorPickerDialog { picked ->
                selectedColor = picked
                colorPreview.setBackgroundColor(picked)
            }
        }

        addButton.setOnClickListener {
            val name = nameField.text.toString().ifBlank { "Untitled" }

            // save category with chosen color
            viewModel.addCategory(Category(name, selectedColor))
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

    private fun showCategoryDetails(category: Category) {
        val idx = viewModel.categories.value?.indexOf(category) ?: -1
        findNavController().navigate(
            R.id.folderDetailsFragment,
            Bundle().apply { putInt("category_index", idx) }
        )
    }

    inner class CategoryAdapter(private var data: MutableList<Category>) :
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
            holder.layout.setBackgroundColor(category.color)

            holder.itemView.setOnClickListener {
                showCategoryDetails(category)
            }
        }

        override fun getItemCount() = data.size

        fun updateData(newData: List<Category>) {
            data.clear()
            data.addAll(newData)
            notifyDataSetChanged()
        }
    }
}