package com.pichler.digitaleshirn.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.pichler.digitaleshirn.data.Entry

class ReminderManager {
    fun scheduleReminder(context: Context, entry: Entry) {
        val triggerAtMillis = resolveTriggerAtMillis(entry) ?: return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, entry)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }

    fun cancelReminder(context: Context, entry: Entry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, entry)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun resolveTriggerAtMillis(entry: Entry): Long? {
        val dueDate = entry.dueDate ?: return null
        val dueTime = entry.dueTime ?: DEFAULT_REMINDER_TIME
        return dueDate + dueTime
    }

    private fun buildPendingIntent(context: Context, entry: Entry): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_ENTRY_ID, entry.id)
            putExtra(EXTRA_ENTRY_TITLE, entry.title)
            putExtra(EXTRA_ENTRY_CATEGORY, entry.category.name)
        }

        return PendingIntent.getBroadcast(
            context,
            entry.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
        const val EXTRA_ENTRY_TITLE = "extra_entry_title"
        const val EXTRA_ENTRY_CATEGORY = "extra_entry_category"
        const val CHANNEL_ID = "digitales_hirn_reminders"
        private const val DEFAULT_REMINDER_TIME = 9L * 60L * 60L * 1000L
    }
}
