package com.pichler.digitaleshirn.data

import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {
    fun getAllEntries(): Flow<List<Entry>> = entryDao.getAllEntries()

    fun getEntriesByCategory(category: Category): Flow<List<Entry>> =
        entryDao.getEntriesByCategory(category)

    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<Entry>> =
        entryDao.getTasksByCompletion(isCompleted)

    fun getOverdueTasks(): Flow<List<Entry>> = entryDao.getOverdueTasks()

    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<Entry>> =
        entryDao.getTodayTasks(startOfDay, endOfDay)

    fun searchEntries(query: String): Flow<List<Entry>> = entryDao.searchEntries(query)

    suspend fun insert(entry: Entry): Long = entryDao.insert(entry)

    suspend fun update(entry: Entry) = entryDao.update(entry)

    suspend fun delete(entry: Entry) = entryDao.delete(entry)

    suspend fun getEntryById(id: Long): Entry? = entryDao.getEntryById(id)

    suspend fun getPendingReminderEntries(): List<Entry> = entryDao.getPendingReminderEntries()
}
