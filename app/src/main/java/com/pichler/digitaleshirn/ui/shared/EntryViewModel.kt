package com.pichler.digitaleshirn.ui.shared

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.pichler.digitaleshirn.classification.ClassificationService
import com.pichler.digitaleshirn.data.Category
import com.pichler.digitaleshirn.data.Entry
import com.pichler.digitaleshirn.data.EntryRepository
import com.pichler.digitaleshirn.reminder.ReminderManager
import com.pichler.digitaleshirn.search.SearchService
import java.util.Calendar

class EntryViewModel(
    application: Application,
    private val repository: EntryRepository,
    private val classificationService: ClassificationService,
    private val searchService: SearchService,
    private val reminderManager: ReminderManager = ReminderManager()
) : AndroidViewModel(application) {

    val allEntries: LiveData<List<Entry>> = repository.getAllEntries().asLiveData()
    val openTasks: LiveData<List<Entry>> = repository.getTasksByCompletion(false).asLiveData()
    val completedTasks: LiveData<List<Entry>> = repository.getTasksByCompletion(true).asLiveData()
    val overdueTasks: LiveData<List<Entry>> = repository.getOverdueTasks().asLiveData()
    val todayTasks: LiveData<List<Entry>> = repository
        .getTodayTasks(todayBounds.first, todayBounds.second)
        .asLiveData()
    val notes: LiveData<List<Entry>> = repository.getEntriesByCategory(Category.NOTE).asLiveData()
    val ideas: LiveData<List<Entry>> = repository.getEntriesByCategory(Category.IDEA).asLiveData()

    suspend fun insertEntry(entry: Entry) {
        val id = repository.insert(entry)
        val savedEntry = entry.copy(id = id)
        if (savedEntry.reminderEnabled && !savedEntry.isCompleted) {
            reminderManager.scheduleReminder(getApplication(), savedEntry)
        }
    }

    suspend fun saveEntry(text: String, overrideCategory: Category? = null) {
        val trimmedText = text.trim()
        if (trimmedText.isEmpty()) {
            return
        }

        val result = classificationService.classify(trimmedText)
        val dueTime = result.dueTime ?: defaultDueTime(result.dueDate, result.reminderEnabled)
        val entry = Entry(
            category = overrideCategory ?: result.category,
            title = result.title.ifBlank { trimmedText.take(80) },
            originalText = trimmedText,
            description = buildDescription(trimmedText, result.title),
            dueDate = result.dueDate,
            dueTime = dueTime,
            reminderEnabled = result.reminderEnabled,
            needsDateCheck = result.needsDateCheck,
            keywords = result.keywords.joinToString(",")
        )

        val id = repository.insert(entry)
        val savedEntry = entry.copy(id = id)
        if (savedEntry.reminderEnabled && !savedEntry.isCompleted) {
            reminderManager.scheduleReminder(getApplication(), savedEntry)
        }
    }

    suspend fun updateEntry(entry: Entry) {
        repository.update(entry)
        if (entry.reminderEnabled && !entry.isCompleted) {
            reminderManager.scheduleReminder(getApplication(), entry)
        } else {
            reminderManager.cancelReminder(getApplication(), entry)
        }
    }

    suspend fun deleteEntry(entry: Entry) {
        reminderManager.cancelReminder(getApplication(), entry)
        repository.delete(entry)
    }

    suspend fun toggleCompleted(entry: Entry) {
        val updatedEntry = entry.copy(isCompleted = !entry.isCompleted)
        updateEntry(updatedEntry)
    }

    suspend fun searchEntries(query: String): List<Entry> = searchService.search(query)

    private fun buildDescription(originalText: String, title: String): String {
        val normalizedTitle = title.trim()
        val normalizedText = originalText.trim()
        return if (normalizedText.equals(normalizedTitle, ignoreCase = true)) {
            ""
        } else {
            normalizedText
        }
    }

    private fun defaultDueTime(dueDate: Long?, reminderEnabled: Boolean): Long? {
        return if (dueDate != null && reminderEnabled) {
            DEFAULT_REMINDER_TIME
        } else {
            null
        }
    }

    companion object {
        private const val DEFAULT_REMINDER_TIME = 9L * 60L * 60L * 1000L

        private val todayBounds: Pair<Long, Long>
            get() {
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                val startOfDay = calendar.timeInMillis
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.add(Calendar.MILLISECOND, -1)
                return startOfDay to calendar.timeInMillis
            }
    }
}
