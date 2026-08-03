package com.pichler.digitaleshirn.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.databinding.FragmentSearchBinding
import com.pichler.digitaleshirn.ui.shared.EditEntryDialog
import com.pichler.digitaleshirn.ui.shared.EntryAdapter
import com.pichler.digitaleshirn.ui.shared.EntryViewModel
import com.pichler.digitaleshirn.ui.shared.EntryViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EntryViewModel by activityViewModels {
        EntryViewModelFactory(requireContext())
    }

    private lateinit var searchAdapter: EntryAdapter
    private var searchJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
    }

    private fun setupRecyclerView() {
        searchAdapter = EntryAdapter(
            onEdit = { entry -> showEditDialog(entry) },
            onDelete = { entry ->
                lifecycleScope.launch { viewModel.deleteEntry(entry) }
            }
        )
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = searchAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s?.toString()?.trim() ?: ""
                performSearch(query)
            }
        })
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        if (query.length < 2) {
            showResults(emptyList())
            return
        }
        searchJob = lifecycleScope.launch {
            delay(300) // debounce
            val results = viewModel.searchEntries(query)
            showResults(results)
        }
    }

    private fun showResults(results: List<Entry>) {
        searchAdapter.submitList(results)
        val isEmpty = results.isEmpty()
        binding.tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.rvResults.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showEditDialog(entry: Entry) {
        EditEntryDialog.newInstance(entry).show(childFragmentManager, "edit")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
