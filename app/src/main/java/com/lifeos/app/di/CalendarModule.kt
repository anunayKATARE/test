package com.lifeos.app.di

import com.lifeos.app.feature.calendar.data.AndroidCalendarRepository
import com.lifeos.app.feature.calendar.domain.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CalendarModule {

    @Binds
    @Singleton
    abstract fun bindCalendarRepository(impl: AndroidCalendarRepository): CalendarRepository
}
