package com.schedule.application

import android.app.Application
import com.schedule.application.worker.ReminderWorker

class ScheduleApplication : Application() {
    val appContainer: AppContainer by lazy {
        AppContainer(this)
    }

    override fun onCreate() {
        super.onCreate()
        ReminderWorker.enqueuePeriodic(this)
    }
}
