package com.pichler.digitaleshirn.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.databinding.FragmentNotesBinding
import com.pichler.digitaleshirn.ui.home.VoiceInputDialog
import com.pichler.digitaleshirn.ui.shared.EditEntryDialog
import com.pichler.digitaleshirn.ui.shared.EntryAdapter
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import kotlinx.coroutines.launch

class NotesFragment : Fragment() {

    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private lateinit var notesAdapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        binding.fabAddNote.setOnClickListener {
            VoiceInputDialog().show(childFragmentManager, "voice_input")
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = EntryAdapter(
            onEdit = { entry -> showEditDialog(entry) },
            onDelete = { entry ->
                lifecycleScope.launch { viewModel.deleteEntry(entry) }
            },
            onConvertToTask = { entry ->
                lifecycleScope.launch {
                    viewModel.updateEntry(entry.copy(category = Category.TASK))
                }
            }
        )
        binding.rvNotes.layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotes.adapter = notesAdapter
    }

    private fun observeData() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            notesAdapter.submitList(notes)
            val isEmpty = notes.isEmpty()
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvNotes.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun showEditDialog(entry: Entry) {
        EditEntryDialog.newInstance(entry).show(childFragmentManager, "edit")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
