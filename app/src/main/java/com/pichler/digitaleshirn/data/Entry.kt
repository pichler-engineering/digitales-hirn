package com.pichler.digitaleshirn.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val category: Category,
    val title: String,
    val originalText: String,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val reminderEnabled: Boolean = false,
    val priority: Int = 1,
    val isCompleted: Boolean = false,
    val needsDateCheck: Boolean = false,
    val keywords: String = "",
    val reminderJobId: String = ""
)
