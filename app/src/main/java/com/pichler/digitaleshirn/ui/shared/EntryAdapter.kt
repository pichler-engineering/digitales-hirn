package com.pichler.digitaleshirn.ui.shared

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.CategorySettingsRepository
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.databinding.ItemEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EntryAdapter(
    private val showCheckbox: Boolean = false,
    private val onToggleComplete: ((Entry) -> Unit)? = null,
    private val onEdit: (Entry) -> Unit,
    private val onDelete: (Entry) -> Unit,
    private val onConvertToTask: ((Entry) -> Unit)? = null
) : ListAdapter<Entry, EntryAdapter.EntryViewHolder>(EntryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val binding = ItemEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EntryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EntryViewHolder(private val binding: ItemEntryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
        private val timeFormat = SimpleDateFormat("HH:mm", Locale.GERMAN)

        fun bind(entry: Entry) {
            val catSettings = CategorySettingsRepository.getInstance(binding.root.context)
            binding.tvTitle.text = entry.title
            binding.tvCategory.text = catSettings.getDisplayName(entry.category)

            // Category dot color
            val dotColor = getCategoryColor(entry.category)
            binding.categoryDot.background.setTint(
                ContextCompat.getColor(binding.root.context, dotColor)
            )

            // Category badge text color
            binding.tvCategory.setTextColor(
                ContextCompat.getColor(binding.root.context, dotColor)
            )

            // Description
            if (entry.description.isNotEmpty()) {
                binding.tvDescription.visibility = View.VISIBLE
                binding.tvDescription.text = entry.description
            } else {
                binding.tvDescription.visibility = View.GONE
            }

            // Checkbox for tasks
            if (showCheckbox && entry.category == Category.TASK) {
                binding.cbCompleted.visibility = View.VISIBLE
                binding.cbCompleted.isChecked = entry.isCompleted
                binding.cbCompleted.setOnCheckedChangeListener(null)
                binding.cbCompleted.setOnCheckedChangeListener { _, _ ->
                    onToggleComplete?.invoke(entry)
                }
                // Strike through completed tasks
                if (entry.isCompleted) {
                    binding.tvTitle.paintFlags = binding.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    binding.tvTitle.alpha = 0.5f
                } else {
                    binding.tvTitle.paintFlags = binding.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    binding.tvTitle.alpha = 1.0f
                }
            } else {
                binding.cbCompleted.visibility = View.GONE
            }

            // Due date
            val dueDate = entry.dueDate
            if (dueDate != null) {
                binding.tvDueDate.visibility = View.VISIBLE
                val dateStr = dateFormat.format(Date(dueDate))
                val timeStr = entry.dueTime?.let { " ${timeFormat.format(Date(it))}" } ?: ""
                val now = System.currentTimeMillis()
                val isOverdue = !entry.isCompleted && dueDate < now
                if (isOverdue) {
                    binding.tvDueDate.text = "⚠ $dateStr$timeStr"
                    binding.tvDueDate.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.red_overdue)
                    )
                } else {
                    binding.tvDueDate.text = "📅 $dateStr$timeStr"
                    binding.tvDueDate.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.text_secondary)
                    )
                }
            } else {
                binding.tvDueDate.visibility = View.GONE
            }

            // Priority indicator
            if (entry.category == Category.TASK && entry.priority != 1) {
                binding.priorityIndicator.visibility = View.VISIBLE
                val priorityColor = when (entry.priority) {
                    0 -> R.color.priority_low
                    2 -> R.color.priority_high
                    else -> R.color.priority_normal
                }
                binding.priorityIndicator.background.setTint(
                    ContextCompat.getColor(binding.root.context, priorityColor)
                )
            } else {
                binding.priorityIndicator.visibility = View.GONE
            }

            // Needs date check
            if (entry.needsDateCheck) {
                binding.tvNeedsDateCheck.visibility = View.VISIBLE
            } else {
                binding.tvNeedsDateCheck.visibility = View.GONE
            }

            // More button popup menu
            binding.btnMore.setOnClickListener { view ->
                showPopupMenu(view, entry)
            }

            // Card click opens edit
            binding.root.setOnClickListener { onEdit(entry) }
        }

        private fun showPopupMenu(view: View, entry: Entry) {
            PopupMenu(view.context, view).apply {
                menu.add(0, 1, 0, view.context.getString(R.string.action_edit))
                if (entry.category != Category.TASK) {
                    onConvertToTask?.let {
                        menu.add(0, 2, 1, view.context.getString(R.string.action_convert_to_task))
                    }
                }
                menu.add(0, 3, 2, view.context.getString(R.string.action_delete))

                setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        1 -> onEdit(entry)
                        2 -> onConvertToTask?.invoke(entry)
                        3 -> onDelete(entry)
                    }
                    true
                }
                show()
            }
        }

        private fun getCategoryColor(category: Category): Int = when (category) {
            Category.TASK -> R.color.category_task
            Category.REMINDER -> R.color.category_reminder
            Category.NOTE -> R.color.category_note
            Category.IDEA -> R.color.category_idea
        }
    }

    private class EntryDiffCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    }
}
