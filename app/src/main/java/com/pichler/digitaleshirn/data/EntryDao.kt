package com.pichler.digitaleshirn.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EntryDao {
    @Query("SELECT * FROM entries ORDER BY createdAt DESC")
    fun getAllEntries(): Flow<List<Entry>>

    @Query("SELECT * FROM entries WHERE category = :category ORDER BY createdAt DESC")
    fun getEntriesByCategory(category: Category): Flow<List<Entry>>

    @Query(
        """
        SELECT * FROM entries
        WHERE category = 'TASK' AND isCompleted = :isCompleted
        ORDER BY CASE WHEN dueDate IS NULL THEN 1 ELSE 0 END, dueDate ASC, dueTime ASC, createdAt DESC
        """
    )
    fun getTasksByCompletion(isCompleted: Boolean): Flow<List<Entry>>

    @Query(
        """
        SELECT * FROM entries
        WHERE category = 'TASK'
          AND isCompleted = 0
          AND dueDate IS NOT NULL
          AND dueDate < CAST(strftime('%s', 'now') AS INTEGER) * 1000
        ORDER BY dueDate ASC, dueTime ASC
        """
    )
    fun getOverdueTasks(): Flow<List<Entry>>

    @Query(
        """
        SELECT * FROM entries
        WHERE category = 'TASK'
          AND isCompleted = 0
          AND dueDate BETWEEN :startOfDay AND :endOfDay
        ORDER BY dueDate ASC, dueTime ASC, createdAt DESC
        """
    )
    fun getTodayTasks(startOfDay: Long, endOfDay: Long): Flow<List<Entry>>

    @Query(
        """
        SELECT * FROM entries
        WHERE title LIKE '%' || :query || '%' COLLATE NOCASE
           OR originalText LIKE '%' || :query || '%' COLLATE NOCASE
           OR description LIKE '%' || :query || '%' COLLATE NOCASE
           OR keywords LIKE '%' || :query || '%' COLLATE NOCASE
        ORDER BY createdAt DESC
        """
    )
    fun searchEntries(query: String): Flow<List<Entry>>

    @Insert
    suspend fun insert(entry: Entry): Long

    @Update
    suspend fun update(entry: Entry)

    @Delete
    suspend fun delete(entry: Entry)

    @Query("SELECT * FROM entries WHERE id = :id LIMIT 1")
    suspend fun getEntryById(id: Long): Entry?

    @Query(
        """
        SELECT * FROM entries
        WHERE reminderEnabled = 1
          AND isCompleted = 0
          AND dueDate IS NOT NULL
        ORDER BY dueDate ASC, dueTime ASC
        """
    )
    suspend fun getPendingReminderEntries(): List<Entry>
}
