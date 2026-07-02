package com.lifeos.app.feature.task.data

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.lifeos.app.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskAlarmReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AlarmEntryPoint {
        fun notificationPoster(): TaskNotificationPoster
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_TASK_ALARM) return
        val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(EXTRA_TITLE) ?: return
        val desc = intent.getStringExtra(EXTRA_DESC) ?: ""
        val body = desc.ifBlank { "Time to start your task!" }

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    AlarmEntryPoint::class.java,
                )
                ep.notificationPoster().post(taskId.hashCode(), title, body)
            } catch (_: Exception) {
                // Last-resort fallback: post directly on the default channel so
                // the notification is never silently dropped
                postFallback(context, taskId.hashCode(), title, body)
            } finally {
                pending.finish()
            }
        }
    }

    private fun postFallback(context: Context, id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java).notify(id, notification)
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
