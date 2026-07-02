package com.lifeos.app.feature.task.domain

interface TaskAlarmScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: String)
}
