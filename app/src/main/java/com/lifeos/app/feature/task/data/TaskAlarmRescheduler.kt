package com.lifeos.app.feature.task.data

import com.lifeos.app.feature.task.domain.TaskAlarmScheduler
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Re-registers AlarmManager alarms for every future-scheduled task.
 *
 * AlarmManager silently drops all of an app's alarms on reboot, app update,
 * and force-stop — so this runs after BOOT_COMPLETED, MY_PACKAGE_REPLACED,
 * and on every app launch. Scheduling is idempotent (FLAG_UPDATE_CURRENT).
 */
@Singleton
class TaskAlarmRescheduler @Inject constructor(
    private val taskDao: TaskDao,
    private val alarmScheduler: TaskAlarmScheduler,
) {
    suspend fun rescheduleAllFuture() {
        taskDao.getFutureScheduledTasks(System.currentTimeMillis()).forEach { entity ->
            alarmScheduler.schedule(entity.toDomain())
        }
    }
}
