package com.lifeos.app.di

import com.lifeos.app.feature.backup.data.BackupRepositoryImpl
import com.lifeos.app.feature.backup.domain.BackupRepository
import com.lifeos.app.feature.category.data.CategoryRepositoryImpl
import com.lifeos.app.feature.category.domain.CategoryRepository
import com.lifeos.app.feature.goal.data.GoalRepositoryImpl
import com.lifeos.app.feature.goal.domain.GoalRepository
import com.lifeos.app.feature.habit.data.HabitRepositoryImpl
import com.lifeos.app.feature.habit.domain.HabitRepository
import com.lifeos.app.feature.inspiration.data.InspirationRepositoryImpl
import com.lifeos.app.feature.inspiration.domain.InspirationRepository
import com.lifeos.app.feature.journal.data.JournalRepositoryImpl
import com.lifeos.app.feature.journal.domain.JournalRepository
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessRepositoryImpl
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessRepository
import com.lifeos.app.feature.mood.data.MoodRepositoryImpl
import com.lifeos.app.feature.mood.domain.MoodRepository
import com.lifeos.app.feature.problemsolver.data.ProblemRepositoryImpl
import com.lifeos.app.feature.problemsolver.domain.ProblemRepository
import com.lifeos.app.feature.reflection.data.ReflectionRepositoryImpl
import com.lifeos.app.feature.reflection.domain.ReflectionRepository
import com.lifeos.app.feature.selfbelief.data.SelfBeliefRepositoryImpl
import com.lifeos.app.feature.selfbelief.domain.SelfBeliefRepository
import com.lifeos.app.feature.task.data.TaskRepositoryImpl
import com.lifeos.app.feature.task.domain.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    @Binds
    @Singleton
    abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindGoalRepository(impl: GoalRepositoryImpl): GoalRepository

    @Binds
    @Singleton
    abstract fun bindMentalToughnessRepository(impl: MentalToughnessRepositoryImpl): MentalToughnessRepository

    @Binds
    @Singleton
    abstract fun bindSelfBeliefRepository(impl: SelfBeliefRepositoryImpl): SelfBeliefRepository

    @Binds
    @Singleton
    abstract fun bindReflectionRepository(impl: ReflectionRepositoryImpl): ReflectionRepository

    @Binds
    @Singleton
    abstract fun bindProblemRepository(impl: ProblemRepositoryImpl): ProblemRepository

    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindInspirationRepository(impl: InspirationRepositoryImpl): InspirationRepository

    @Binds
    @Singleton
    abstract fun bindBackupRepository(impl: BackupRepositoryImpl): BackupRepository
}
