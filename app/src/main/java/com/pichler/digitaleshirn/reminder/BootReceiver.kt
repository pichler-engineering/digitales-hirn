package com.pichler.digitaleshirn.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.pichler.digitaleshirn.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val entryDao = AppDatabase.getInstance(appContext).entryDao()
                val reminderManager = ReminderManager()
                entryDao.getPendingReminderEntries().forEach { entry ->
                    reminderManager.scheduleReminder(appContext, entry)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
