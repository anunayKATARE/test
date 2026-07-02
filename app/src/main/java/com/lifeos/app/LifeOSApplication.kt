package com.lifeos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.lifeos.app.feature.task.data.TaskAlarmReceiver
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val channel = NotificationChannel(
            TaskAlarmReceiver.CHANNEL_ID,
            TaskAlarmReceiver.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifications when your scheduled tasks are about to start"
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }
}
