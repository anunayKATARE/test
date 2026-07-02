package com.lifeos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.lifeos.app.feature.settings.domain.SoundProfile
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LifeOSApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val nm = getSystemService(NotificationManager::class.java)

        val soundAndVibrateChannel = NotificationChannel(
            SoundProfile.SOUND_AND_VIBRATE.channelId,
            "Task Reminders (Sound & Vibrate)",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Task alarms with sound and vibration"
            enableVibration(true)
        }

        val vibrateOnlyChannel = NotificationChannel(
            SoundProfile.VIBRATE_ONLY.channelId,
            "Task Reminders (Vibrate Only)",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Task alarms with vibration only, no sound"
            enableVibration(true)
            setSound(null, null)
        }

        val silentChannel = NotificationChannel(
            SoundProfile.SILENT.channelId,
            "Task Reminders (Silent)",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Silent task alarms"
            enableVibration(false)
            setSound(null, null)
        }

        nm.createNotificationChannels(listOf(soundAndVibrateChannel, vibrateOnlyChannel, silentChannel))
    }
}
