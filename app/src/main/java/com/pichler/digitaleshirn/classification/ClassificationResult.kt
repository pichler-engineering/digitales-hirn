package com.pichler.digitaleshirn.classification

import com.pichler.digitaleshirn.data.Category

data class ClassificationResult(
    val category: Category,
    val title: String,
    val keywords: List<String>,
    val dueDate: Long?,
    val dueTime: Long?,
    val needsDateCheck: Boolean,
    val reminderEnabled: Boolean
)
