package com.lifeos.app.di

import com.lifeos.app.feature.task.data.TaskAlarmSchedulerImpl
import com.lifeos.app.feature.task.domain.TaskAlarmScheduler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindTaskAlarmScheduler(impl: TaskAlarmSchedulerImpl): TaskAlarmScheduler
}
