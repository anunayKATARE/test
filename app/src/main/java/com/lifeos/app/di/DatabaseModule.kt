package com.lifeos.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lifeos.app.core.database.AppDatabase
import com.lifeos.app.feature.category.data.CategoryDao
import com.lifeos.app.feature.checkin.data.CheckInDao
import com.lifeos.app.feature.goal.data.GoalDao
import com.lifeos.app.feature.habit.data.HabitDao
import com.lifeos.app.feature.inspiration.data.InspirationDao
import com.lifeos.app.feature.journal.data.JournalDao
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessDao
import com.lifeos.app.feature.mood.data.MoodDao
import com.lifeos.app.feature.problemsolver.data.ProblemDao
import com.lifeos.app.feature.reflection.data.ReflectionDao
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDao
import com.lifeos.app.feature.task.data.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            for (table in listOf(
                "categories", "journal_entries", "mood_entries", "habits", "habit_completions",
                "goals", "mental_toughness_entries", "self_belief_reflections", "reflection_entries",
                "problems", "tasks", "inspiration_items",
            )) {
                db.execSQL("ALTER TABLE $table ADD COLUMN profileId TEXT")
            }
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS check_in_sessions (
                    id TEXT NOT NULL PRIMARY KEY,
                    scheduledAt INTEGER NOT NULL,
                    completedAt INTEGER,
                    profileId TEXT
                )""",
            )
            db.execSQL(
                """CREATE TABLE IF NOT EXISTS check_in_commitments (
                    id TEXT NOT NULL PRIMARY KEY,
                    sessionId TEXT NOT NULL,
                    text TEXT NOT NULL,
                    linkedId TEXT,
                    linkedType TEXT,
                    isCompleted INTEGER NOT NULL DEFAULT 0,
                    sortOrder INTEGER NOT NULL DEFAULT 0
                )""",
            )
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
            .fallbackToDestructiveMigration()
            .build()

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

    @Provides
    fun provideTaskDao(db: AppDatabase): TaskDao = db.taskDao()

    @Provides
    fun provideInspirationDao(db: AppDatabase): InspirationDao = db.inspirationDao()

    @Provides
    fun provideCheckInDao(db: AppDatabase): CheckInDao = db.checkInDao()
}
