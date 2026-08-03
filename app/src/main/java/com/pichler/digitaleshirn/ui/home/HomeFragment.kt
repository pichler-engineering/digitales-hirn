package com.pichler.digitaleshirn.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.databinding.FragmentHomeBinding
import com.pichler.digitaleshirn.ui.shared.EditEntryDialog
import com.pichler.digitaleshirn.ui.shared.EntryAdapter
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private lateinit var todayAdapter: EntryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        todayAdapter = EntryAdapter(
            showCheckbox = true,
            onToggleComplete = { entry ->
                lifecycleScope.launch { viewModel.toggleCompleted(entry) }
            },
            onEdit = { entry ->
                EditEntryDialog.newInstance(entry).show(childFragmentManager, "edit")
            },
            onDelete = { entry ->
                lifecycleScope.launch { viewModel.deleteEntry(entry) }
            }
        )
        binding.rvTodayEntries.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTodayEntries.adapter = todayAdapter
    }

    private fun observeData() {
        viewModel.todayTasks.observe(viewLifecycleOwner) { entries ->
            todayAdapter.submitList(entries)
            binding.tvEmptyToday.visibility = if (entries.isEmpty()) View.VISIBLE else View.GONE
            binding.rvTodayEntries.visibility = if (entries.isEmpty()) View.GONE else View.VISIBLE
        }

        viewModel.openTasks.observe(viewLifecycleOwner) { tasks ->
            val count = tasks.size
            binding.tvTaskCount.text = "$count offen"
        }

        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            binding.tvNoteCount.text = "${notes.size} Notizen"
        }

        viewModel.ideas.observe(viewLifecycleOwner) { ideas ->
            binding.tvIdeaCount.text = "${ideas.size} Ideen"
        }
    }

    private fun setupClickListeners() {
        binding.btnMic.setOnClickListener {
            VoiceInputDialog().show(childFragmentManager, "voice_input")
        }

        binding.tileAufgaben.setOnClickListener {
            findNavController().navigate(R.id.tasksFragment)
        }

        binding.tileNotizen.setOnClickListener {
            findNavController().navigate(R.id.notesFragment)
        }

        binding.tileIdeen.setOnClickListener {
            findNavController().navigate(R.id.ideasFragment)
        }

        binding.tileSuche.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
