package com.lifeos.app.di

import com.lifeos.app.feature.settings.data.NotificationPrefsRepositoryImpl
import com.lifeos.app.feature.settings.domain.NotificationPrefsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationPrefsModule {

    @Binds
    @Singleton
    abstract fun bindNotificationPrefsRepository(impl: NotificationPrefsRepositoryImpl): NotificationPrefsRepository
}
