package com.lifeos.app.feature.task.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Re-registers task alarms after events that clear AlarmManager state:
 * device reboot (BOOT_COMPLETED) and app update (MY_PACKAGE_REPLACED).
 */
class TaskBootReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface BootEntryPoint {
        fun alarmRescheduler(): TaskAlarmRescheduler
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    BootEntryPoint::class.java,
                ).alarmRescheduler().rescheduleAllFuture()
            } finally {
                pending.finish()
            }
        }
    }
}
