package at.pichler.digitaleshirn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import at.pichler.digitaleshirn.data.dao.InboxDao
import at.pichler.digitaleshirn.data.dao.NoteDao
import at.pichler.digitaleshirn.data.dao.ProjectDao
import at.pichler.digitaleshirn.data.dao.TaskDao
import at.pichler.digitaleshirn.data.entity.InboxEntryEntity
import at.pichler.digitaleshirn.data.entity.NoteEntity
import at.pichler.digitaleshirn.data.entity.ProjectEntity
import at.pichler.digitaleshirn.data.entity.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

@Database(
    entities = [TaskEntity::class, InboxEntryEntity::class, ProjectEntity::class, NoteEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun inboxDao(): InboxDao
    abstract fun projectDao(): ProjectDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digitales_hirn.db"
                ).addCallback(DatabaseSeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}

private class DatabaseSeedCallback : RoomDatabase.Callback() {
    private val defaults = listOf(
        "Forsttech",
        "Seilkran",
        "Pichler.one",
        "Digitales Hirn",
        "Tiny House",
        "Ernährung",
        "Privat"
    )

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            defaults.forEach { name ->
                db.execSQL(
                    "INSERT INTO projects(name, createdAt) VALUES (?, ?)",
                    arrayOf(name, LocalDateTime.now().toString())
                )
            }
        }
    }
}
