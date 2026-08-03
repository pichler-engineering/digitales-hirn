package at.pichler.digitaleshirn.reminder

import at.pichler.digitaleshirn.data.entity.TaskEntity

interface ReminderScheduler {
    fun schedule(task: TaskEntity)
    fun cancel(taskId: Long)
}
