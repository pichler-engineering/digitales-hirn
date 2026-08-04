package com.pichler.digitaleshirn.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.chip.Chip
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.classification.GermanDateTimeParser
import com.pichler.digitaleshirn.classification.RuleBasedClassificationService
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.CategorySettingsRepository
import com.pichler.digitaleshirn.databinding.DialogVoiceInputBinding
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.voice.VoiceInputHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VoiceInputDialog : DialogFragment() {

    private var _binding: DialogVoiceInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private val voiceInputHelper = VoiceInputHelper()
    private var selectedCategory: Category = Category.TASK
    private var selectedDateMillis: Long? = null
    private var selectedTimeMillis: Long? = null

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)

    private val speechLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val text = voiceInputHelper.parseResult(result.data)
            if (text != null) {
                onSpeechResult(text)
            } else {
                binding.tvStatus.text = getString(R.string.speech_error)
            }
        } else {
            binding.tvStatus.text = getString(R.string.tap_to_start)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogVoiceInputBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryChips()
        setupButtons()
    }

    private fun setupCategoryChips() {
        val catSettings = CategorySettingsRepository.getInstance(requireContext())
        val chips = listOf(
            binding.chipTask to Category.TASK,
            binding.chipReminder to Category.REMINDER,
            binding.chipNote to Category.NOTE,
            binding.chipIdea to Category.IDEA
        )
        chips.forEach { (chip, cat) ->
            chip.text = catSettings.getDisplayName(cat)
            chip.setOnClickListener { selectCategory(cat) }
        }
        selectCategory(Category.TASK)
    }

    private fun selectCategory(category: Category) {
        selectedCategory = category
        val chips = listOf(
            binding.chipTask to Category.TASK,
            binding.chipReminder to Category.REMINDER,
            binding.chipNote to Category.NOTE,
            binding.chipIdea to Category.IDEA
        )
        chips.forEach { (chip, cat) ->
            chip.isChecked = (cat == category)
        }
    }

    private fun setupButtons() {
        binding.fabMic.setOnClickListener {
            startVoiceInput()
        }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnDate.setOnClickListener { showDatePicker() }
        binding.btnTime.setOnClickListener { showTimePicker() }

        binding.etRecognizedText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val hasText = !s.isNullOrBlank()
                binding.btnSave.isEnabled = hasText
                if (hasText) {
                    binding.layoutDateTime.visibility = View.VISIBLE
                    binding.layoutPriority.visibility = View.VISIBLE
                    binding.switchReminder.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun startVoiceInput() {
        val intent = voiceInputHelper.createSpeechIntent() ?: run {
            Toast.makeText(requireContext(), getString(R.string.speech_not_available), Toast.LENGTH_SHORT).show()
            return
        }
        binding.tvStatus.text = getString(R.string.listening)
        try {
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            binding.tvStatus.text = getString(R.string.speech_error)
        }
    }

    private fun onSpeechResult(text: String) {
        binding.etRecognizedText.setText(text)
        binding.tvStatus.text = getString(R.string.recognized_text_label)

        lifecycleScope.launch {
            try {
                val catSettings = CategorySettingsRepository.getInstance(requireContext())
                val service = RuleBasedClassificationService(categorySettings = catSettings)
                val result = service.classify(text)

                // Update category chip selection
                selectCategory(result.category)

                // Update date/time if detected
                result.dueDate?.let { date ->
                    selectedDateMillis = date
                    binding.btnDate.text = dateFormat.format(date)
                }
                result.dueTime?.let { time ->
                    selectedTimeMillis = time
                    val cal = Calendar.getInstance().apply { timeInMillis = time }
                    binding.btnTime.text = timeFormat.format(time)
                }

                // Update reminder switch
                binding.switchReminder.isChecked = result.reminderEnabled

                // Update the text field with the stripped title (keyword removed)
                val stripped = result.title
                if (stripped.isNotBlank() && stripped != text) {
                    binding.etRecognizedText.setText(stripped)
                }
            } catch (_: Exception) {}
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        selectedDateMillis?.let { cal.timeInMillis = it }

        android.app.DatePickerDialog(
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
        selectedTimeMillis?.let { cal.timeInMillis = it }

        android.app.TimePickerDialog(
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

    private fun saveEntry() {
        val text = binding.etRecognizedText.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return

        lifecycleScope.launch {
            try {
                val entry = com.pichler.digitaleshirn.data.Entry(
                    category = selectedCategory,
                    title = text.take(80),
                    originalText = text,
                    dueDate = selectedDateMillis,
                    dueTime = selectedTimeMillis,
                    reminderEnabled = binding.switchReminder.isChecked
                )
                viewModel.insertEntry(entry)
                Toast.makeText(requireContext(), getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show()
                dismiss()
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Fehler beim Speichern", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
