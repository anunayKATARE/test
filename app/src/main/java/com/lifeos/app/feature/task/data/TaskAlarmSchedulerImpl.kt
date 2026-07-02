package com.lifeos.app.feature.task.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.lifeos.app.feature.task.domain.Task
import com.lifeos.app.feature.task.domain.TaskAlarmScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAlarmSchedulerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : TaskAlarmScheduler {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task) {
        val at = task.scheduledAt ?: return
        if (at.isBefore(Instant.now())) return
        val pending = buildPendingIntent(task.id, task.title, task.description)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pending)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pending)
        }
    }

    override fun cancel(taskId: String) {
        alarmManager.cancel(buildPendingIntent(taskId, "", ""))
    }

    private fun buildPendingIntent(taskId: String, title: String, desc: String): PendingIntent {
        val intent = Intent(context, TaskAlarmReceiver::class.java).apply {
            action = TaskAlarmReceiver.ACTION_TASK_ALARM
            putExtra(TaskAlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(TaskAlarmReceiver.EXTRA_TITLE, title)
            putExtra(TaskAlarmReceiver.EXTRA_DESC, desc)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
