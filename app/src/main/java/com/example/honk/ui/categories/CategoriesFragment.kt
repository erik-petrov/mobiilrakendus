package com.example.honk.ui.categories

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
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

        // Observe data
        viewModel.categories.observe(viewLifecycleOwner) { updated ->
            adapter.updateData(updated)
        }

        // Initialize default categories if empty
        if (viewModel.categories.value.isNullOrEmpty()) {
            viewModel.categories.value = mutableListOf(
                Category("SCHOOL", R.color.category_red),
                Category("PETS", R.color.category_green),
                Category("HOLIDAY", R.color.category_yellow),
                Category("WORK", R.color.category_blue)
            )
        }

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

            viewModel.addCategory(Category(name, colorRes))
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showCategoryDetails(category: Category) {
        val index = viewModel.categories.value?.indexOf(category) ?: -1
        val bundle = Bundle().apply {
            putInt("category_index", index)
        }
        findNavController().navigate(R.id.folderDetailsFragment, bundle)
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
            holder.layout.setBackgroundResource(category.color)
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
