package com.pichler.digitaleshirn.ui.home

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.databinding.DialogVoiceInputBinding
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.voice.VoiceInputHelper
import kotlinx.coroutines.launch

class VoiceInputDialog : DialogFragment() {

    private var _binding: DialogVoiceInputBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private val voiceInputHelper = VoiceInputHelper()

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
        setupCategorySpinner()
        setupButtons()
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

        binding.etRecognizedText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                val hasText = !s.isNullOrBlank()
                binding.btnSave.isEnabled = hasText
                if (hasText) {
                    binding.layoutCategory.visibility = View.VISIBLE
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

        // Auto-classify in background
        lifecycleScope.launch {
            try {
                val categoryIndex = binding.spinnerCategory.selectedItemPosition
                val category = Category.values()[categoryIndex]
                binding.spinnerCategory.setSelection(Category.values().indexOf(category))
            } catch (_: Exception) {}
        }
    }

    private fun saveEntry() {
        val text = binding.etRecognizedText.text?.toString()?.trim() ?: return
        if (text.isEmpty()) return

        val categoryIndex = binding.spinnerCategory.selectedItemPosition
        val overrideCategory = if (categoryIndex >= 0) Category.values()[categoryIndex] else null

        lifecycleScope.launch {
            try {
                viewModel.saveEntry(text, overrideCategory)
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
