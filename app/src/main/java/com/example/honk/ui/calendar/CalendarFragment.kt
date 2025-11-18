package com.example.honk.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.honk.R
import com.example.honk.model.Reminder
import java.text.SimpleDateFormat
import java.util.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.widget.NestedScrollView

import android.widget.ImageView
import android.net.Uri
import java.util.Locale

import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts

import android.os.Environment
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import android.Manifest
import android.content.pm.PackageManager
import java.io.File


class CalendarFragment : Fragment() {

    private var pendingImageUri: Uri? = null
    private var currentImagePreview: ImageView? = null

    private lateinit var calendarGrid: GridView
    private lateinit var monthLabel: TextView
    private lateinit var reminderList: RecyclerView
    private lateinit var reminderAdapter: ReminderAdapter

    private val reminders = mutableListOf<Reminder>()
    private var selectedDate: String? = null
    private var currentCalendar: Calendar = Calendar.getInstance()

    // choosing photo from gallery
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                pendingImageUri = uri
                currentImagePreview?.apply {
                    visibility = View.VISIBLE
                    setImageURI(uri)
                }
            }
        }

    // taking a photo
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success && pendingImageUri != null) {
                currentImagePreview?.apply {
                    visibility = View.VISIBLE
                    setImageURI(pendingImageUri)
                }
            } else {
                pendingImageUri = null
            }
        }

    private val requestCameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openCamera()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Camera permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private fun openCamera() {
        val uri = createImageUri()
        if (uri == null) {
            Toast.makeText(requireContext(), "Cannot create image file", Toast.LENGTH_SHORT).show()
            return
        }
        pendingImageUri = uri
        takePictureLauncher.launch(uri)
    }


    private fun createImageUri(): Uri? {
        val context = requireContext()
        val imagesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: return null

        val imageFile = File(
            imagesDir,
            "reminder_${System.currentTimeMillis()}.jpg"
        )

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.calendar_fragment, container, false)

        // --- Filter button ---
        val filterButton = view.findViewById<ImageButton>(R.id.filterButton)
        filterButton.setOnClickListener { showFilterDialog() }

        // --- Setup calendar views ---
        calendarGrid = view.findViewById(R.id.calendarGrid)
        monthLabel = view.findViewById(R.id.monthLabel)

        updateCalendar()

        // --- Month navigation buttons ---
        view.findViewById<View>(R.id.prevMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        view.findViewById<View>(R.id.nextMonthBtn).setOnClickListener {
            currentCalendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        // --- Day click listener ---
        calendarGrid.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val day = calendarGrid.adapter.getItem(position) as String
                if (day.isNotBlank()) {
                    val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
                    selectedDate = "${sdf.format(currentCalendar.time)}-${day.padStart(2, '0')}"
                    view.findViewById<TextView>(R.id.selectedDateLabel).text =
                        "$day ${monthLabel.text}"
                    updateReminderList()
                }
            }

        // --- RecyclerView setup ---
        reminderList = view.findViewById(R.id.reminderList)
        reminderAdapter = ReminderAdapter(
            onReminderClick = { reminder ->
                showEditReminderDialog(reminder)
            },
            onDeleteClick = { reminder ->
                reminders.remove(reminder)
                updateReminderList()
            }
        )

        reminderList.layoutManager = LinearLayoutManager(requireContext())
        reminderList.adapter = reminderAdapter

        // --- FAB (add reminder) ---
        val fabAdd = view.findViewById<View>(R.id.fab_add_reminder)
        fabAdd.setOnClickListener { showAddReminderDialog() }

        return view
    }

    // --- Filter popup dialog ---
    private fun showFilterDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filter, null)
        val checkAll = dialogView.findViewById<CheckBox>(R.id.checkAll)
        val checkWork = dialogView.findViewById<CheckBox>(R.id.checkWork)
        val checkPet = dialogView.findViewById<CheckBox>(R.id.checkPet)
        val checkSchool = dialogView.findViewById<CheckBox>(R.id.checkSchool)
        val checkHome = dialogView.findViewById<CheckBox>(R.id.checkHome)

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_HONK_Dialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        var isUpdating = false

        // Clicking 'All themes' changes others themes behaviour
        checkAll.setOnCheckedChangeListener { _, isChecked ->
            if (isUpdating) return@setOnCheckedChangeListener
            isUpdating = true
            if (isChecked) {
                listOf(checkWork, checkPet, checkSchool, checkHome).forEach {
                    it.isChecked = false
                }
            } else {
                listOf(checkWork, checkPet, checkSchool, checkHome).forEach {
                    it.isEnabled = true
                }
            }
            isUpdating = false
        }

        // Clicking other themes changes 'All themes' behavious
        val themeCheckChangeListener = CompoundButton.OnCheckedChangeListener { _, _ ->
            if (isUpdating) return@OnCheckedChangeListener
            isUpdating = true
            if (checkWork.isChecked || checkPet.isChecked || checkSchool.isChecked || checkHome.isChecked) {
                checkAll.isChecked = false
            }
            isUpdating = false
        }

        checkWork.setOnCheckedChangeListener(themeCheckChangeListener)
        checkPet.setOnCheckedChangeListener(themeCheckChangeListener)
        checkSchool.setOnCheckedChangeListener(themeCheckChangeListener)
        checkHome.setOnCheckedChangeListener(themeCheckChangeListener)

    }

    // --- Calendar update logic ---
    private fun updateCalendar() {
        val tempCalendar = currentCalendar.clone() as Calendar
        tempCalendar.set(Calendar.DAY_OF_MONTH, 1)
        var firstDayOfWeek = tempCalendar.get(Calendar.DAY_OF_WEEK) - 2
        if (firstDayOfWeek < 0) {
            firstDayOfWeek += 7
        }
        tempCalendar.add(Calendar.MONTH, -1)
        val prevMonthDays = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        tempCalendar.add(Calendar.MONTH, 1)

        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        monthLabel.text = monthFormat.format(currentCalendar.time)
        val daysInMonth = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val days = mutableListOf<String>()
        if (firstDayOfWeek != 0) {
            for (i in prevMonthDays - firstDayOfWeek + 1..prevMonthDays) {
                days.add(i.toString())
            }
        }
        for (d in 1..daysInMonth) days.add(d.toString())
        calendarGrid.adapter = DaysAdapter(days)
    }

    // --- Add Reminder dialog ---
    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val reminderText = dialogView.findViewById<EditText>(R.id.reminderText)
        val reminderTime = dialogView.findViewById<EditText>(R.id.reminderTime)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addReminderButton)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.reminderImagePreview)
        val choosePhotoButton = dialogView.findViewById<Button>(R.id.buttonChoosePhoto)
        val takePhotoButton = dialogView.findViewById<Button>(R.id.buttonTakePhoto)

        currentImagePreview = imagePreview
        pendingImageUri = null
        imagePreview.visibility = View.GONE

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_HONK_Dialog)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        choosePhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        takePhotoButton.setOnClickListener {
            val context = requireContext()
            val permission = Manifest.permission.CAMERA
            when {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                    // if we have permission
                    openCamera()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    requestCameraPermissionLauncher.launch(permission)
                }
                else -> {
                    // just request permission
                    requestCameraPermissionLauncher.launch(permission)
                }
            }
        }


        addButton.setOnClickListener {
            val date = selectedDate
            val category = categorySpinner.selectedItem.toString()
            if (date != null && reminderText.text.isNotBlank()) {
                val reminder = Reminder(
                    date = date,
                    time = reminderTime.text.toString(),
                    text = reminderText.text.toString(),
                    category = category,
                    imageUri = pendingImageUri?.toString()
                )
                reminders.add(reminder)
                updateReminderList()
                dialog.dismiss()
            } else {
                Toast.makeText(requireContext(), "Select a date first", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    // --- Update visible reminders ---
    private fun updateReminderList() {
        val currentDate = selectedDate ?: return
        val filtered = reminders.filter { it.date == currentDate }
        reminderAdapter.setReminders(filtered)
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)
        val reminderText = dialogView.findViewById<EditText>(R.id.reminderText)
        val reminderTime = dialogView.findViewById<EditText>(R.id.reminderTime)
        val categorySpinner = dialogView.findViewById<Spinner>(R.id.categorySpinner)
        val addButton = dialogView.findViewById<Button>(R.id.addReminderButton)
        val imagePreview = dialogView.findViewById<ImageView>(R.id.reminderImagePreview)
        val choosePhotoButton = dialogView.findViewById<Button>(R.id.buttonChoosePhoto)
        val takePhotoButton = dialogView.findViewById<Button>(R.id.buttonTakePhoto)

        dialogView.findViewById<TextView>(R.id.dialogTitle).text = "Edit Reminder"
        // Pre-fill existing values
        reminderText.setText(reminder.text)
        reminderTime.setText(reminder.time)
        addButton.text = "Save Changes"

        currentImagePreview = imagePreview
        if (!reminder.imageUri.isNullOrEmpty()) {
            pendingImageUri = Uri.parse(reminder.imageUri)
            imagePreview.visibility = View.VISIBLE
            imagePreview.setImageURI(pendingImageUri)
        } else {
            pendingImageUri = null
            imagePreview.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(requireContext(), R.style.Theme_HONK_Dialog)
            .setView(dialogView)
            .setCancelable(true)
            .setNegativeButton("Delete") { _, _ ->
                reminders.remove(reminder)
                updateReminderList()
            }
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        choosePhotoButton.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        takePhotoButton.setOnClickListener {
            val context = requireContext()
            val permission = Manifest.permission.CAMERA
            when {
                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                    openCamera()
                }
                shouldShowRequestPermissionRationale(permission) -> {
                    requestCameraPermissionLauncher.launch(permission)
                }
                else -> {
                    requestCameraPermissionLauncher.launch(permission)
                }
            }
        }


        addButton.setOnClickListener {
            reminder.text = reminderText.text.toString()
            reminder.time = reminderTime.text.toString()
            reminder.category = categorySpinner.selectedItem.toString()
            reminder.imageUri = pendingImageUri?.toString()
            updateReminderList()
            dialog.dismiss()
        }

        dialog.show()
    }

    // --- Adapter for calendar day cells ---
    inner class DaysAdapter(private val days: List<String>) : BaseAdapter() {
        override fun getCount() = days.size
        override fun getItem(position: Int) = days[position]
        override fun getItemId(position: Int) = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: layoutInflater.inflate(R.layout.item_day, parent, false)
            val dayText = view.findViewById<TextView>(R.id.dayText) ?: view as TextView
            dayText.text = days[position]

            val today = Calendar.getInstance()
            if (
                days[position].isNotBlank() &&
                currentCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentCalendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                days[position].toInt() == today.get(Calendar.DAY_OF_MONTH)
            ) {
                dayText.setBackgroundResource(R.drawable.day_cell_today_bg)
            } else {
                dayText.setBackgroundResource(R.drawable.day_cell_bg)
            }

            return view
        }
    }

    // --- Adapter for reminders list ---
    inner class ReminderAdapter(
        private val onReminderClick: (Reminder) -> Unit,
        private val onDeleteClick: (Reminder) -> Unit
    ) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

        private var reminderItems = listOf<Reminder>()

        fun setReminders(newReminders: List<Reminder>) {
            reminderItems = newReminders
            notifyDataSetChanged()
        }

        inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val text: TextView = view.findViewById(R.id.reminderText)
            val image: ImageView = view.findViewById(R.id.reminderImage)
            val deleteButton: ImageButton = view.findViewById(R.id.deleteButton)

            init {
                view.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onReminderClick(reminderItems[pos])
                    }
                }

                deleteButton.setOnClickListener {
                    val pos = adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        onDeleteClick(reminderItems[pos])
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_reminder, parent, false)
            return ViewHolder(view)
        }

        override fun getItemCount() = reminderItems.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val r = reminderItems[position]
            holder.text.text = "${r.time} – ${r.text} (${r.category})"

            val color = when (r.category.uppercase()) {
                "WORK" -> 0xFF03A9F4.toInt()
                "SCHOOL" -> 0xFF4CAF50.toInt()
                "PET" -> 0xFFF44336.toInt()
                "HOME" -> 0xFF9C27B0.toInt()
                else -> 0xFF888888.toInt()
            }
            holder.text.setTextColor(color)
            holder.image.visibility = View.GONE
        }
    }

    fun onClick_fab_add_remainder(view: View?){

    }

}
