package com.pichler.digitaleshirn.ui.shared

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.databinding.DialogEditEntryBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditEntryDialog : DialogFragment() {

    private var _binding: DialogEditEntryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private var existingEntry: Entry? = null
    private var selectedDateMillis: Long? = null
    private var selectedTimeMillis: Long? = null
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogEditEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).also {
            it.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCategorySpinner()
        setupPriorityChips()
        loadExistingEntry()
        setupButtons()
    }

    fun setEntry(entry: Entry) {
        existingEntry = entry
    }

    private fun setupCategorySpinner() {
        val categories = Category.values().map { it.displayName }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categories
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerCategory.adapter = adapter
    }

    private fun setupPriorityChips() {
        // Default selection is Normal
    }

    private fun loadExistingEntry() {
        val entry = existingEntry ?: return
        binding.etTitle.setText(entry.title)
        binding.etDescription.setText(entry.description)
        binding.spinnerCategory.setSelection(Category.values().indexOf(entry.category))
        binding.switchReminder.isChecked = entry.reminderEnabled
        binding.etKeywords.setText(entry.keywords.replace(",", ", "))

        entry.dueDate?.let {
            selectedDateMillis = it
            binding.btnDate.text = dateFormat.format(it)
        }
        entry.dueTime?.let {
            selectedTimeMillis = it
            binding.btnTime.text = timeFormat.format(it)
        }

        when (entry.priority) {
            0 -> binding.chipLow.isChecked = true
            1 -> binding.chipNormal.isChecked = true
            2 -> binding.chipHigh.isChecked = true
        }
    }

    private fun setupButtons() {
        binding.btnDate.setOnClickListener { showDatePicker() }
        binding.btnTime.setOnClickListener { showTimePicker() }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDateMillis?.let { cal.timeInMillis = it }

        DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, dayOfMonth, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                selectedDateMillis = selected.timeInMillis
                binding.btnDate.text = dateFormat.format(selected.time)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        val cal = Calendar.getInstance()
        selectedTimeMillis?.let {
            cal.timeInMillis = it
        }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val timeMillis = (hourOfDay * 60L + minute) * 60L * 1000L
                selectedTimeMillis = timeMillis
                binding.btnTime.text = String.format(Locale.GERMAN, "%02d:%02d", hourOfDay, minute)
            },
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun getPriority(): Int {
        return when {
            binding.chipLow.isChecked -> 0
            binding.chipHigh.isChecked -> 2
            else -> 1
        }
    }

    private fun saveEntry() {
        val title = binding.etTitle.text?.toString()?.trim() ?: return
        if (title.isEmpty()) {
            binding.etTitle.error = "Titel eingeben"
            return
        }

        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val category = Category.values()[categoryIndex]
        val description = binding.etDescription.text?.toString()?.trim() ?: ""
        val keywords = binding.etKeywords.text?.toString()?.trim() ?: ""
        val reminderEnabled = binding.switchReminder.isChecked
        val priority = getPriority()

        lifecycleScope.launch {
            val existing = existingEntry
            if (existing != null) {
                val updated = existing.copy(
                    title = title,
                    description = description,
                    category = category,
                    dueDate = selectedDateMillis,
                    dueTime = selectedTimeMillis,
                    reminderEnabled = reminderEnabled,
                    priority = priority,
                    keywords = keywords.replace(", ", ",")
                )
                viewModel.updateEntry(updated)
            } else {
                val entry = com.pichler.digitaleshirn.data.Entry(
                    category = category,
                    title = title,
                    originalText = title,
                    description = description,
                    dueDate = selectedDateMillis,
                    dueTime = selectedTimeMillis,
                    reminderEnabled = reminderEnabled,
                    priority = priority,
                    keywords = keywords.replace(", ", ",")
                )
                viewModel.insertEntry(entry)
            }
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(entry: Entry? = null): EditEntryDialog {
            return EditEntryDialog().apply {
                entry?.let { setEntry(it) }
            }
        }
    }
}
