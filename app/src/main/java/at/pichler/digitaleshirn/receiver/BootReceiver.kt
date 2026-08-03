package at.pichler.digitaleshirn.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import at.pichler.digitaleshirn.data.AppDatabase
import at.pichler.digitaleshirn.reminder.AlarmReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.get(context)
                val scheduler = AlarmReminderScheduler(context)
                db.taskDao().getReminderCandidates().forEach { scheduler.schedule(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
