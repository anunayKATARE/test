package com.lifeos.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.lifeos.app.core.demo.DemoModeManager
import com.lifeos.app.core.demo.DemoModeManagerImpl
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.core.demo.DemoModeRepositoryImpl
import com.lifeos.app.core.demo.DemoSeeder
import com.lifeos.app.feature.category.data.CategoryDemoSeeder
import com.lifeos.app.feature.goal.data.GoalDemoSeeder
import com.lifeos.app.feature.habit.data.HabitDemoSeeder
import com.lifeos.app.feature.inspiration.data.InspirationDemoSeeder
import com.lifeos.app.feature.journal.data.JournalDemoSeeder
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessDemoSeeder
import com.lifeos.app.feature.mood.data.MoodDemoSeeder
import com.lifeos.app.feature.problemsolver.data.ProblemDemoSeeder
import com.lifeos.app.feature.reflection.data.ReflectionDemoSeeder
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDemoSeeder
import com.lifeos.app.feature.task.data.TaskDemoSeeder
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DemoModule {

    @Binds
    @Singleton
    abstract fun bindDemoModeRepository(impl: DemoModeRepositoryImpl): DemoModeRepository

    @Binds
    @Singleton
    abstract fun bindDemoModeManager(impl: DemoModeManagerImpl): DemoModeManager

    @Binds
    @IntoSet
    abstract fun bindGoalDemoSeeder(impl: GoalDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindCategoryDemoSeeder(impl: CategoryDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindHabitDemoSeeder(impl: HabitDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindMoodDemoSeeder(impl: MoodDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindJournalDemoSeeder(impl: JournalDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindProblemDemoSeeder(impl: ProblemDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindTaskDemoSeeder(impl: TaskDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindInspirationDemoSeeder(impl: InspirationDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindMentalToughnessDemoSeeder(impl: MentalToughnessDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindSelfBeliefDemoSeeder(impl: SelfBeliefDemoSeeder): DemoSeeder

    @Binds
    @IntoSet
    abstract fun bindReflectionDemoSeeder(impl: ReflectionDemoSeeder): DemoSeeder

    companion object {
        @Provides
        @Singleton
        fun provideDemoModePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("demo_mode_prefs") }
    }
}
