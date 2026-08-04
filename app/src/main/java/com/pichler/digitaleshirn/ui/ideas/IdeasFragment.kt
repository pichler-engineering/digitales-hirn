package com.pichler.digitaleshirn.ui.ideas

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
import com.pichler.digitaleshirn.databinding.FragmentIdeasBinding
import com.pichler.digitaleshirn.ui.home.VoiceInputDialog
import com.pichler.digitaleshirn.ui.shared.EditEntryDialog
import com.pichler.digitaleshirn.ui.shared.EntryAdapter
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import kotlinx.coroutines.launch

class IdeasFragment : Fragment() {

    private var _binding: FragmentIdeasBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private lateinit var ideasAdapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdeasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeData()
        binding.fabAddIdea.setOnClickListener {
            VoiceInputDialog().show(childFragmentManager, "voice_input")
        }
    }

    private fun setupRecyclerView() {
        ideasAdapter = EntryAdapter(
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
        binding.rvIdeas.layoutManager = LinearLayoutManager(requireContext())
        binding.rvIdeas.adapter = ideasAdapter
    }

    private fun observeData() {
        viewModel.ideas.observe(viewLifecycleOwner) { ideas ->
            ideasAdapter.submitList(ideas)
            val isEmpty = ideas.isEmpty()
            binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.rvIdeas.visibility = if (isEmpty) View.GONE else View.VISIBLE
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
