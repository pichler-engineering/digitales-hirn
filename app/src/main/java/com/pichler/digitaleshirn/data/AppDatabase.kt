package com.pichler.digitaleshirn.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Entry::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /** Migrates stored German enum names to internal English names. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("UPDATE entries SET category = 'TASK' WHERE category = 'AUFGABE'")
                db.execSQL("UPDATE entries SET category = 'REMINDER' WHERE category = 'ERINNERUNG'")
                db.execSQL("UPDATE entries SET category = 'NOTE' WHERE category = 'NOTIZ'")
                db.execSQL("UPDATE entries SET category = 'IDEA' WHERE category = 'IDEE'")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "digitales_hirn.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
