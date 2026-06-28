package com.lifeos.app.di

import com.lifeos.app.ai.AiInsightProvider
import com.lifeos.app.ai.NoOpAiInsightProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindAiInsightProvider(impl: NoOpAiInsightProvider): AiInsightProvider
}
