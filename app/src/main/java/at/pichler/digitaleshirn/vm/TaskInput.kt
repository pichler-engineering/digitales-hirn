package at.pichler.digitaleshirn.vm

import at.pichler.digitaleshirn.model.Priority
import java.time.LocalDate
import java.time.LocalTime

data class TaskInput(
    val title: String,
    val description: String? = null,
    val projectId: Long? = null,
    val priority: Priority = Priority.NORMAL,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val reminderEnabled: Boolean = false,
    val completed: Boolean = false
)
