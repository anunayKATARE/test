package com.lifeos.app.feature.task.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.lifeos.app.feature.task.domain.TaskAlarmScheduler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskBootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun taskDao(): TaskDao
        fun alarmScheduler(): TaskAlarmScheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ep = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootEntryPoint::class.java,
                )
                val nowMillis = System.currentTimeMillis()
                ep.taskDao().getFutureScheduledTasks(nowMillis).forEach { entity ->
                    ep.alarmScheduler().schedule(entity.toDomain())
                }
            } finally {
                pending.finish()
            }
        }
    }
}
