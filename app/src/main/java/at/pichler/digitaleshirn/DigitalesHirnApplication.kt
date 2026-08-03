package at.pichler.digitaleshirn

import android.app.Application
import at.pichler.digitaleshirn.data.AppDatabase
import at.pichler.digitaleshirn.reminder.AlarmReminderScheduler
import at.pichler.digitaleshirn.reminder.NotificationHelper
import at.pichler.digitaleshirn.repository.AppRepository

class DigitalesHirnApplication : Application() {
    lateinit var repository: AppRepository
    lateinit var reminderScheduler: AlarmReminderScheduler

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = AppRepository(db)
        reminderScheduler = AlarmReminderScheduler(this)
        NotificationHelper.createChannel(this)
    }
}
