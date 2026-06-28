package com.lifeos.app.feature.habit.data

import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitCompletion
import com.lifeos.app.feature.habit.domain.HabitRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
) : HabitRepository {

    override fun observeActiveHabits(): Flow<List<Habit>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override fun observeAllHabits(): Flow<List<Habit>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getHabit(id: String): Habit? = dao.getById(id)?.toDomain()

    override suspend fun upsertHabit(habit: Habit) = dao.upsertHabit(habit.toEntity())

    override suspend fun deleteHabit(id: String) {
        dao.deleteCompletionsForHabit(id)
        dao.deleteHabit(id)
    }

    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived)

    override fun observeCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>> =
        dao.observeCompletionsForHabit(habitId).map { list -> list.map { it.toDomain() } }

    override fun observeCompletionsOn(date: LocalDate): Flow<List<HabitCompletion>> =
        dao.observeCompletionsOn(date.toEpochDay()).map { list -> list.map { it.toDomain() } }

    override suspend fun getCompletionsBetween(habitId: String, start: LocalDate, end: LocalDate): List<HabitCompletion> =
        dao.getCompletionsBetween(habitId, start.toEpochDay(), end.toEpochDay()).map { it.toDomain() }

    override suspend fun setCompletion(habitId: String, date: LocalDate, completed: Boolean, note: String) {
        val existing = dao.getCompletion(habitId, date.toEpochDay())
        val entity = HabitCompletionEntity(
            id = existing?.id ?: "${habitId}_${date}",
            habitId = habitId,
            date = date,
            completed = completed,
            note = note,
        )
        dao.upsertCompletion(entity)
    }

    override suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.completionCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }
}
