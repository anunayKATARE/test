package com.lifeos.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.lifeos.app.feature.category.data.CategoryDao
import com.lifeos.app.feature.category.data.CategoryEntity
import com.lifeos.app.feature.goal.data.GoalDao
import com.lifeos.app.feature.goal.data.GoalEntity
import com.lifeos.app.feature.habit.data.HabitCompletionEntity
import com.lifeos.app.feature.habit.data.HabitDao
import com.lifeos.app.feature.habit.data.HabitEntity
import com.lifeos.app.feature.inspiration.data.InspirationDao
import com.lifeos.app.feature.inspiration.data.InspirationEntity
import com.lifeos.app.feature.journal.data.JournalDao
import com.lifeos.app.feature.journal.data.JournalEntryEntity
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessDao
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessEntity
import com.lifeos.app.feature.mood.data.MoodDao
import com.lifeos.app.feature.mood.data.MoodEntryEntity
import com.lifeos.app.feature.plan.data.DayPlanDao
import com.lifeos.app.feature.plan.data.DayPlanEntity
import com.lifeos.app.feature.problemsolver.data.ProblemDao
import com.lifeos.app.feature.problemsolver.data.ProblemEntity
import com.lifeos.app.feature.reflection.data.ReflectionDao
import com.lifeos.app.feature.reflection.data.ReflectionEntity
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDao
import com.lifeos.app.feature.selfbelief.data.SelfBeliefEntity
import com.lifeos.app.feature.checkin.data.CheckInCommitmentEntity
import com.lifeos.app.feature.checkin.data.CheckInDao
import com.lifeos.app.feature.checkin.data.CheckInSessionEntity
import com.lifeos.app.feature.profile.data.ProfileDao
import com.lifeos.app.feature.profile.data.ProfileEntity
import com.lifeos.app.feature.task.data.TaskDao
import com.lifeos.app.feature.task.data.TaskEntity
import com.lifeos.app.feature.timelog.data.TimeLogDao
import com.lifeos.app.feature.timelog.data.TimeLogEntity

@Database(
    entities = [
        CategoryEntity::class,
        JournalEntryEntity::class,
        MoodEntryEntity::class,
        HabitEntity::class,
        HabitCompletionEntity::class,
        GoalEntity::class,
        MentalToughnessEntity::class,
        SelfBeliefEntity::class,
        ReflectionEntity::class,
        ProblemEntity::class,
        TaskEntity::class,
        InspirationEntity::class,
        CheckInSessionEntity::class,
        CheckInCommitmentEntity::class,
        ProfileEntity::class,
        DayPlanEntity::class,
        TimeLogEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun journalDao(): JournalDao
    abstract fun moodDao(): MoodDao
    abstract fun habitDao(): HabitDao
    abstract fun goalDao(): GoalDao
    abstract fun mentalToughnessDao(): MentalToughnessDao
    abstract fun selfBeliefDao(): SelfBeliefDao
    abstract fun reflectionDao(): ReflectionDao
    abstract fun problemDao(): ProblemDao
    abstract fun taskDao(): TaskDao
    abstract fun inspirationDao(): InspirationDao
    abstract fun checkInDao(): CheckInDao
    abstract fun profileDao(): ProfileDao
    abstract fun dayPlanDao(): DayPlanDao
    abstract fun timeLogDao(): TimeLogDao

    companion object {
        const val DATABASE_NAME = "lifeos.db"
    }
}
