package com.pichler.digitaleshirn.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pichler.digitaleshirn.R
import com.pichler.digitaleshirn.data.Category

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        createNotificationChannel(context)

        val entryId = intent?.getLongExtra(ReminderManager.EXTRA_ENTRY_ID, 0L) ?: 0L
        val title = intent?.getStringExtra(ReminderManager.EXTRA_ENTRY_TITLE).orEmpty().ifBlank {
            context.getString(R.string.app_name)
        }
        val categoryName = intent?.getStringExtra(ReminderManager.EXTRA_ENTRY_CATEGORY)
        val category = categoryName?.let { runCatching { Category.valueOf(it) }.getOrNull() }
        val contentText = category?.defaultName?.let { "Kategorie: $it" } ?: "Erinnerung"

        val notification = NotificationCompat.Builder(context, ReminderManager.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(entryId.toInt(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }

        val channel = NotificationChannel(
            ReminderManager.CHANNEL_ID,
            context.getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Benachrichtigungen für Erinnerungen und Aufgaben"
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
