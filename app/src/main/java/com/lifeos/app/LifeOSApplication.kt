package com.lifeos.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import com.lifeos.app.feature.settings.domain.SoundProfile
import com.lifeos.app.feature.task.data.TaskAlarmRescheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class LifeOSApplication : Application() {

    @Inject lateinit var alarmRescheduler: TaskAlarmRescheduler

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // Safety net: alarms are cleared by app updates and force-stops, so
        // re-register every future task reminder on each launch (idempotent)
        applicationScope.launch {
            runCatching { alarmRescheduler.rescheduleAllFuture() }
        }

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
