package com.lifeos.app.feature.task.data

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lifeos.app.R
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmEntryPoint {
        fun notificationPrefsRepository(): NotificationPrefsRepository
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TASK_ALARM) return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val desc = intent.getStringExtra(EXTRA_DESC) ?: ""

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmEntryPoint::class.java,
                )
                val profile = ep.notificationPrefsRepository().observeSoundProfile().first()
                val bodyText = desc.ifBlank { "Time to start your task!" }

                val notification = NotificationCompat.Builder(context, profile.channelId)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(bodyText)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_REMINDER)
                    .setAutoCancel(true)
                    .extend(NotificationCompat.WearableExtender())
                    .build()

                context.getSystemService(NotificationManager::class.java)
                    .notify(taskId.hashCode(), notification)
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ACTION_TASK_ALARM = "com.lifeos.app.ACTION_TASK_ALARM"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_TITLE = "task_title"
        const val EXTRA_DESC = "task_desc"
        const val CHANNEL_ID = "task_alarms"
        const val CHANNEL_NAME = "Task Reminders"
    }
}
