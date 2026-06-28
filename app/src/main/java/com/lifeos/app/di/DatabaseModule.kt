package com.lifeos.app.di

import android.content.Context
import androidx.room.Room
import com.lifeos.app.core.database.AppDatabase
import com.lifeos.app.feature.category.data.CategoryDao
import com.lifeos.app.feature.goal.data.GoalDao
import com.lifeos.app.feature.habit.data.HabitDao
import com.lifeos.app.feature.journal.data.JournalDao
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessDao
import com.lifeos.app.feature.mood.data.MoodDao
import com.lifeos.app.feature.problemsolver.data.ProblemDao
import com.lifeos.app.feature.reflection.data.ReflectionDao
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME).build()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()

    @Provides
    fun provideHabitDao(db: AppDatabase): HabitDao = db.habitDao()

    @Provides
    fun provideGoalDao(db: AppDatabase): GoalDao = db.goalDao()

    @Provides
    fun provideMentalToughnessDao(db: AppDatabase): MentalToughnessDao = db.mentalToughnessDao()

    @Provides
    fun provideSelfBeliefDao(db: AppDatabase): SelfBeliefDao = db.selfBeliefDao()

    @Provides
    fun provideReflectionDao(db: AppDatabase): ReflectionDao = db.reflectionDao()

    @Provides
    fun provideProblemDao(db: AppDatabase): ProblemDao = db.problemDao()
}
