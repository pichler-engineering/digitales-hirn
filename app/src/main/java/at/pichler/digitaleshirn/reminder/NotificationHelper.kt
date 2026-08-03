package at.pichler.digitaleshirn.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "digitales_hirn_erinnerungen"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Erinnerungen",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Aufgaben-Erinnerungen"
        }
        manager.createNotificationChannel(channel)
    }
}
