package com.pichler.digitaleshirn.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.databinding.FragmentTasksBinding
import com.pichler.digitaleshirn.ui.home.VoiceInputDialog
import com.pichler.digitaleshirn.ui.shared.EditEntryDialog
import com.pichler.digitaleshirn.ui.shared.EntryAdapter
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import kotlinx.coroutines.launch

class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private lateinit var taskAdapter: EntryAdapter
    private var currentTab = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        observeData()
        binding.fabAddTask.setOnClickListener {
            VoiceInputDialog().show(childFragmentManager, "voice_input")
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Offen"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Heute"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Überfällig"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Erledigt"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentTab = tab.position
                refreshList()
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    private fun setupRecyclerView() {
        taskAdapter = EntryAdapter(
            showCheckbox = true,
            onToggleComplete = { entry ->
                lifecycleScope.launch { viewModel.toggleCompleted(entry) }
            },
            onEdit = { entry -> showEditDialog(entry) },
            onDelete = { entry ->
                lifecycleScope.launch { viewModel.deleteEntry(entry) }
            }
        )
        binding.rvTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTasks.adapter = taskAdapter
    }

    private fun observeData() {
        viewModel.openTasks.observe(viewLifecycleOwner) { if (currentTab == 0) showTasks(it) }
        viewModel.todayTasks.observe(viewLifecycleOwner) { if (currentTab == 1) showTasks(it) }
        viewModel.overdueTasks.observe(viewLifecycleOwner) { if (currentTab == 2) showTasks(it) }
        viewModel.completedTasks.observe(viewLifecycleOwner) { if (currentTab == 3) showTasks(it) }
    }

    private fun refreshList() {
        when (currentTab) {
            0 -> viewModel.openTasks.value?.let { showTasks(it) }
            1 -> viewModel.todayTasks.value?.let { showTasks(it) }
            2 -> viewModel.overdueTasks.value?.let { showTasks(it) }
            3 -> viewModel.completedTasks.value?.let { showTasks(it) }
        }
    }

    private fun showTasks(tasks: List<Entry>) {
        taskAdapter.submitList(tasks)
        val isEmpty = tasks.isEmpty()
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvTasks.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showEditDialog(entry: Entry) {
        EditEntryDialog.newInstance(entry).show(childFragmentManager, "edit")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
