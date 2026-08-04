package com.pichler.digitaleshirn.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.CategorySettingsRepository
import com.pichler.digitaleshirn.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadNames()
        setupButtons()
    }

    private fun loadNames() {
        val repo = CategorySettingsRepository.getInstance(requireContext())
        binding.etNameTask.setText(repo.getDisplayName(Category.TASK))
        binding.etNameReminder.setText(repo.getDisplayName(Category.REMINDER))
        binding.etNameNote.setText(repo.getDisplayName(Category.NOTE))
        binding.etNameIdea.setText(repo.getDisplayName(Category.IDEA))
    }

    private fun setupButtons() {
        binding.btnSaveNames.setOnClickListener { saveNames() }

        binding.btnResetNames.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.settings_reset_title)
                .setMessage(R.string.settings_reset_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    CategorySettingsRepository.getInstance(requireContext()).resetToDefaults()
                    loadNames()
                    Toast.makeText(requireContext(), R.string.settings_reset_done, Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.no, null)
                .show()
        }
    }

    private fun saveNames() {
        val nameTask = binding.etNameTask.text?.toString()?.trim() ?: ""
        val nameReminder = binding.etNameReminder.text?.toString()?.trim() ?: ""
        val nameNote = binding.etNameNote.text?.toString()?.trim() ?: ""
        val nameIdea = binding.etNameIdea.text?.toString()?.trim() ?: ""

        // Validate not empty
        if (nameTask.isEmpty() || nameReminder.isEmpty() || nameNote.isEmpty() || nameIdea.isEmpty()) {
            Toast.makeText(requireContext(), R.string.settings_name_empty_error, Toast.LENGTH_SHORT).show()
            return
        }

        // Validate no duplicates
        val names = listOf(nameTask, nameReminder, nameNote, nameIdea)
        if (names.size != names.map { it.lowercase() }.distinct().size) {
            Toast.makeText(requireContext(), R.string.settings_name_duplicate_error, Toast.LENGTH_SHORT).show()
            return
        }

        val repo = CategorySettingsRepository.getInstance(requireContext())
        repo.setDisplayName(Category.TASK, nameTask)
        repo.setDisplayName(Category.REMINDER, nameReminder)
        repo.setDisplayName(Category.NOTE, nameNote)
        repo.setDisplayName(Category.IDEA, nameIdea)

        Toast.makeText(requireContext(), R.string.settings_saved, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
