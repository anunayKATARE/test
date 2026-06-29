package com.lifeos.app.feature.habit.data

import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.habit.domain.Habit
import com.lifeos.app.feature.habit.domain.HabitCompletion
import com.lifeos.app.feature.habit.domain.HabitRepository
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class HabitRepositoryImpl @Inject constructor(
    private val dao: HabitDao,
    private val demoModeRepository: DemoModeRepository,
) : HabitRepository {

    private fun Flow<List<HabitEntity>>.filterByActiveProfile(): Flow<List<HabitEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    private fun Flow<List<HabitCompletionEntity>>.filterCompletionsByActiveProfile(): Flow<List<HabitCompletionEntity>> =
        combine(demoModeRepository.activeProfile) { entities, profile -> entities.filter { it.profileId == profile?.id } }

    override fun observeActiveHabits(): Flow<List<Habit>> =
        dao.observeActive().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeAllHabits(): Flow<List<Habit>> =
        dao.observeAll().filterByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getHabit(id: String): Habit? = dao.getById(id)?.toDomain()

    override suspend fun upsertHabit(habit: Habit) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        dao.upsertHabit(habit.toEntity().copy(profileId = profileId))
    }

    override suspend fun deleteHabit(id: String) {
        dao.deleteCompletionsForHabit(id)
        dao.deleteHabit(id)
    }

    override suspend fun setArchived(id: String, archived: Boolean) = dao.setArchived(id, archived)

    override fun observeCompletionsForHabit(habitId: String): Flow<List<HabitCompletion>> =
        dao.observeCompletionsForHabit(habitId).filterCompletionsByActiveProfile().map { list -> list.map { it.toDomain() } }

    override fun observeCompletionsOn(date: LocalDate): Flow<List<HabitCompletion>> =
        dao.observeCompletionsOn(date.toEpochDay()).filterCompletionsByActiveProfile().map { list -> list.map { it.toDomain() } }

    override suspend fun getCompletionsBetween(habitId: String, start: LocalDate, end: LocalDate): List<HabitCompletion> =
        dao.getCompletionsBetween(habitId, start.toEpochDay(), end.toEpochDay()).map { it.toDomain() }

    override suspend fun setCompletion(habitId: String, date: LocalDate, completed: Boolean, note: String) {
        val existing = dao.getCompletion(habitId, date.toEpochDay())
        val profileId = demoModeRepository.activeProfile.first()?.id
        val entity = HabitCompletionEntity(
            id = existing?.id ?: "${habitId}_${date}",
            habitId = habitId,
            date = date,
            completed = completed,
            note = note,
            profileId = profileId,
        )
        dao.upsertCompletion(entity)
    }

    override suspend fun completionCountByDay(start: LocalDate, end: LocalDate): Map<Long, Int> =
        dao.completionCountByDay(start.toEpochDay(), end.toEpochDay()).associate { it.day to it.count }
}
