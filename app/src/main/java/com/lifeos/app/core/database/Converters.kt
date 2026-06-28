package com.lifeos.app.core.database

import androidx.room.TypeConverter
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalStatus
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.inspiration.domain.InspirationType
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import java.time.Instant
import java.time.LocalDate

/**
 * Centralised Room type converters. List/Map fields are stored as delimited strings rather than
 * JSON to avoid pulling in a serialization dependency for simple flat collections of strings.
 */
class Converters {

    @TypeConverter
    fun instantToEpochMillis(instant: Instant?): Long? = instant?.toEpochMilli()

    @TypeConverter
    fun epochMillisToInstant(millis: Long?): Instant? = millis?.let { Instant.ofEpochMilli(it) }

    @TypeConverter
    fun localDateToEpochDay(date: LocalDate?): Long? = date?.toEpochDay()

    @TypeConverter
    fun epochDayToLocalDate(epochDay: Long?): LocalDate? = epochDay?.let { LocalDate.ofEpochDay(it) }

    @TypeConverter
    fun stringListToString(list: List<String>?): String? = list?.joinToString(UNIT_SEPARATOR)

    @TypeConverter
    fun stringToStringList(value: String?): List<String> =
        if (value.isNullOrEmpty()) emptyList() else value.split(UNIT_SEPARATOR)

    @TypeConverter
    fun intListToString(list: List<Int>?): String? = list?.joinToString(UNIT_SEPARATOR)

    @TypeConverter
    fun stringToIntList(value: String?): List<Int> =
        if (value.isNullOrEmpty()) emptyList() else value.split(UNIT_SEPARATOR).map { it.toInt() }

    @TypeConverter
    fun stringMapToString(map: Map<String, String>?): String? =
        map?.entries?.joinToString(RECORD_SEPARATOR) { "${it.key}$KEY_VALUE_SEPARATOR${it.value}" }

    @TypeConverter
    fun stringToStringMap(value: String?): Map<String, String> {
        if (value.isNullOrEmpty()) return emptyMap()
        return value.split(RECORD_SEPARATOR).associate { pair ->
            val parts = pair.split(KEY_VALUE_SEPARATOR, limit = 2)
            parts[0] to parts.getOrElse(1) { "" }
        }
    }

    @TypeConverter
    fun emotionToString(value: Emotion): String = value.name

    @TypeConverter
    fun stringToEmotion(value: String): Emotion = Emotion.valueOf(value)

    @TypeConverter
    fun habitScheduleTypeToString(value: HabitScheduleType): String = value.name

    @TypeConverter
    fun stringToHabitScheduleType(value: String): HabitScheduleType = HabitScheduleType.valueOf(value)

    @TypeConverter
    fun habitDifficultyToString(value: HabitDifficulty): String = value.name

    @TypeConverter
    fun stringToHabitDifficulty(value: String): HabitDifficulty = HabitDifficulty.valueOf(value)

    @TypeConverter
    fun habitImportanceToString(value: HabitImportance): String = value.name

    @TypeConverter
    fun stringToHabitImportance(value: String): HabitImportance = HabitImportance.valueOf(value)

    @TypeConverter
    fun goalHorizonToString(value: GoalHorizon): String = value.name

    @TypeConverter
    fun stringToGoalHorizon(value: String): GoalHorizon = GoalHorizon.valueOf(value)

    @TypeConverter
    fun goalStatusToString(value: GoalStatus): String = value.name

    @TypeConverter
    fun stringToGoalStatus(value: String): GoalStatus = GoalStatus.valueOf(value)

    @TypeConverter
    fun mentalToughnessTypeToString(value: MentalToughnessType): String = value.name

    @TypeConverter
    fun stringToMentalToughnessType(value: String): MentalToughnessType = MentalToughnessType.valueOf(value)

    @TypeConverter
    fun reflectionTemplateTypeToString(value: ReflectionTemplateType): String = value.name

    @TypeConverter
    fun stringToReflectionTemplateType(value: String): ReflectionTemplateType = ReflectionTemplateType.valueOf(value)

    @TypeConverter
    fun problemStatusToString(value: ProblemStatus): String = value.name

    @TypeConverter
    fun stringToProblemStatus(value: String): ProblemStatus = ProblemStatus.valueOf(value)

    @TypeConverter
    fun inspirationTypeToString(value: InspirationType): String = value.name

    @TypeConverter
    fun stringToInspirationType(value: String): InspirationType = InspirationType.valueOf(value)

    companion object {
        private const val UNIT_SEPARATOR = ""
        private const val RECORD_SEPARATOR = ""
        private const val KEY_VALUE_SEPARATOR = ""
    }
}
