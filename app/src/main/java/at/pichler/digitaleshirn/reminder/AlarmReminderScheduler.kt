package at.pichler.digitaleshirn.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import at.pichler.digitaleshirn.data.entity.TaskEntity
import at.pichler.digitaleshirn.receiver.TaskReminderReceiver
import java.time.ZoneId

class AlarmReminderScheduler(private val context: Context) : ReminderScheduler {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(task: TaskEntity) {
        val date = task.dueDate ?: return
        val time = task.dueTime ?: return
        if (!task.reminderEnabled || task.completed) return
        val trigger = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        if (trigger <= System.currentTimeMillis()) return

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            trigger,
            createPendingIntent(task.id, task.title)
        )
    }

    override fun cancel(taskId: Long) {
        alarmManager.cancel(createPendingIntent(taskId, ""))
    }

    private fun createPendingIntent(taskId: Long, title: String): PendingIntent {
        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskReminderReceiver.EXTRA_TASK_TITLE, title)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
