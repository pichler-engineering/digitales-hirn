package at.pichler.digitaleshirn.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import at.pichler.digitaleshirn.data.entity.InboxEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {
    @Query("SELECT * FROM inbox_entries ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<InboxEntryEntity>>

    @Query("SELECT * FROM inbox_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): InboxEntryEntity?

    @Insert
    suspend fun insert(entry: InboxEntryEntity): Long

    @Update
    suspend fun update(entry: InboxEntryEntity)

    @Delete
    suspend fun delete(entry: InboxEntryEntity)
}
