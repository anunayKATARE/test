package com.lifeos.app.feature.backup.data

import com.lifeos.app.feature.backup.domain.BackupRepository
import com.lifeos.app.feature.category.data.CategoryDao
import com.lifeos.app.feature.category.data.CategoryEntity
import com.lifeos.app.feature.goal.data.GoalDao
import com.lifeos.app.feature.goal.data.GoalEntity
import com.lifeos.app.feature.goal.domain.GoalHorizon
import com.lifeos.app.feature.goal.domain.GoalStatus
import com.lifeos.app.feature.habit.data.HabitCompletionEntity
import com.lifeos.app.feature.habit.data.HabitDao
import com.lifeos.app.feature.habit.data.HabitEntity
import com.lifeos.app.feature.habit.domain.HabitDifficulty
import com.lifeos.app.feature.habit.domain.HabitImportance
import com.lifeos.app.feature.habit.domain.HabitScheduleType
import com.lifeos.app.feature.inspiration.data.InspirationDao
import com.lifeos.app.feature.inspiration.data.InspirationEntity
import com.lifeos.app.feature.inspiration.domain.InspirationType
import com.lifeos.app.feature.journal.data.JournalDao
import com.lifeos.app.feature.journal.data.JournalEntryEntity
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessDao
import com.lifeos.app.feature.mentaltoughness.data.MentalToughnessEntity
import com.lifeos.app.feature.mentaltoughness.domain.MentalToughnessType
import com.lifeos.app.feature.mood.data.MoodDao
import com.lifeos.app.feature.mood.data.MoodEntryEntity
import com.lifeos.app.feature.mood.domain.Emotion
import com.lifeos.app.feature.problemsolver.data.ProblemDao
import com.lifeos.app.feature.problemsolver.data.ProblemEntity
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import com.lifeos.app.feature.reflection.data.ReflectionDao
import com.lifeos.app.feature.reflection.data.ReflectionEntity
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDao
import com.lifeos.app.feature.selfbelief.data.SelfBeliefEntity
import com.lifeos.app.feature.task.data.TaskDao
import com.lifeos.app.feature.task.data.TaskEntity
import com.lifeos.app.core.demo.DemoModeRepository
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private const val SCHEMA_VERSION = 1

class BackupRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val journalDao: JournalDao,
    private val moodDao: MoodDao,
    private val habitDao: HabitDao,
    private val goalDao: GoalDao,
    private val mentalToughnessDao: MentalToughnessDao,
    private val selfBeliefDao: SelfBeliefDao,
    private val reflectionDao: ReflectionDao,
    private val problemDao: ProblemDao,
    private val taskDao: TaskDao,
    private val inspirationDao: InspirationDao,
    private val demoModeRepository: DemoModeRepository,
) : BackupRepository {

    override suspend fun exportSnapshot(): String {
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", Instant.now().toEpochMilli())
        root.put("categories", JSONArray(categoryDao.getAllReal().map { it.toJson() }))
        root.put("journalEntries", JSONArray(journalDao.getAllReal().map { it.toJson() }))
        root.put("moodEntries", JSONArray(moodDao.getAllReal().map { it.toJson() }))
        root.put("habits", JSONArray(habitDao.getAllRealHabits().map { it.toJson() }))
        root.put("habitCompletions", JSONArray(habitDao.getAllRealCompletions().map { it.toJson() }))
        root.put("goals", JSONArray(goalDao.getAllReal().map { it.toJson() }))
        root.put("mentalToughnessEntries", JSONArray(mentalToughnessDao.getAllReal().map { it.toJson() }))
        root.put("selfBeliefReflections", JSONArray(selfBeliefDao.getAllReal().map { it.toJson() }))
        root.put("reflectionEntries", JSONArray(reflectionDao.getAllReal().map { it.toJson() }))
        root.put("problems", JSONArray(problemDao.getAllReal().map { it.toJson() }))
        root.put("tasks", JSONArray(taskDao.getAllReal().map { it.toJson() }))
        root.put("inspirationItems", JSONArray(inspirationDao.getAllReal().map { it.toJson() }))
        return root.toString()
    }

    override suspend fun importSnapshot(json: String) {
        // All imported rows must carry the current profile ID or they are invisible to every
        // repository query (which filters by profileId == activeProfile.id).
        val profileId = demoModeRepository.activeProfile.first()?.id
        val root = JSONObject(json)

        categoryDao.deleteAllReal()
        root.getJSONArray("categories").toObjects()
            .forEach { categoryDao.upsert(it.toCategoryEntity().copy(profileId = profileId)) }

        journalDao.deleteAllReal()
        root.getJSONArray("journalEntries").toObjects()
            .forEach { journalDao.upsert(it.toJournalEntryEntity().copy(profileId = profileId)) }

        moodDao.deleteAllReal()
        root.getJSONArray("moodEntries").toObjects()
            .forEach { moodDao.upsert(it.toMoodEntryEntity().copy(profileId = profileId)) }

        habitDao.deleteAllRealHabits()
        root.getJSONArray("habits").toObjects()
            .forEach { habitDao.upsertHabit(it.toHabitEntity().copy(profileId = profileId)) }

        habitDao.deleteAllRealCompletions()
        root.getJSONArray("habitCompletions").toObjects()
            .forEach { habitDao.upsertCompletion(it.toHabitCompletionEntity().copy(profileId = profileId)) }

        goalDao.deleteAllReal()
        root.getJSONArray("goals").toObjects()
            .forEach { goalDao.upsert(it.toGoalEntity().copy(profileId = profileId)) }

        mentalToughnessDao.deleteAllReal()
        root.getJSONArray("mentalToughnessEntries").toObjects()
            .forEach { mentalToughnessDao.upsert(it.toMentalToughnessEntity().copy(profileId = profileId)) }

        selfBeliefDao.deleteAllReal()
        root.getJSONArray("selfBeliefReflections").toObjects()
            .forEach { selfBeliefDao.upsert(it.toSelfBeliefEntity().copy(profileId = profileId)) }

        reflectionDao.deleteAllReal()
        root.getJSONArray("reflectionEntries").toObjects()
            .forEach { reflectionDao.upsert(it.toReflectionEntity().copy(profileId = profileId)) }

        problemDao.deleteAllReal()
        root.getJSONArray("problems").toObjects()
            .forEach { problemDao.upsert(it.toProblemEntity().copy(profileId = profileId)) }

        taskDao.deleteAllReal()
        root.getJSONArray("tasks").toObjects()
            .forEach { taskDao.upsertTask(it.toTaskEntity().copy(profileId = profileId)) }

        inspirationDao.deleteAllReal()
        root.getJSONArray("inspirationItems").toObjects()
            .forEach { inspirationDao.upsert(it.toInspirationEntity().copy(profileId = profileId)) }
    }
}

private fun JSONArray.toObjects(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }

private fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }

private fun JSONArray.toIntList(): List<Int> = (0 until length()).map { getInt(it) }

private fun stringListJson(list: List<String>): JSONArray = JSONArray(list)

private fun intListJson(list: List<Int>): JSONArray = JSONArray(list)

private fun JSONObject.optStringOrNull(name: String): String? = if (isNull(name)) null else optString(name)

private fun CategoryEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("description", description)
    put("colorHex", colorHex)
    put("icon", icon)
    put("orderIndex", orderIndex)
    put("isArchived", isArchived)
    put("isHidden", isHidden)
    put("createdAt", createdAt.toEpochMilli())
}

private fun JSONObject.toCategoryEntity() = CategoryEntity(
    id = getString("id"),
    name = getString("name"),
    description = getString("description"),
    colorHex = getString("colorHex"),
    icon = getString("icon"),
    orderIndex = getInt("orderIndex"),
    isArchived = getBoolean("isArchived"),
    isHidden = getBoolean("isHidden"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = null,
)

private fun JournalEntryEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("dateTime", dateTime.toEpochMilli())
    put("title", title)
    put("body", body)
    put("tags", stringListJson(tags))
    put("categoryId", categoryId)
    put("moodEntryId", moodEntryId)
    put("goalIds", stringListJson(goalIds))
    put("habitIds", stringListJson(habitIds))
    put("peopleMentioned", stringListJson(peopleMentioned))
    put("location", location)
    put("weather", weather)
    put("imageUris", stringListJson(imageUris))
    put("voiceNoteUris", stringListJson(voiceNoteUris))
    put("createdAt", createdAt.toEpochMilli())
    put("updatedAt", updatedAt.toEpochMilli())
}

private fun JSONObject.toJournalEntryEntity() = JournalEntryEntity(
    id = getString("id"),
    dateTime = Instant.ofEpochMilli(getLong("dateTime")),
    title = getString("title"),
    body = getString("body"),
    tags = getJSONArray("tags").toStringList(),
    categoryId = optStringOrNull("categoryId"),
    moodEntryId = optStringOrNull("moodEntryId"),
    goalIds = getJSONArray("goalIds").toStringList(),
    habitIds = getJSONArray("habitIds").toStringList(),
    peopleMentioned = getJSONArray("peopleMentioned").toStringList(),
    location = optStringOrNull("location"),
    weather = optStringOrNull("weather"),
    imageUris = getJSONArray("imageUris").toStringList(),
    voiceNoteUris = getJSONArray("voiceNoteUris").toStringList(),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    updatedAt = Instant.ofEpochMilli(getLong("updatedAt")),
    profileId = null,
)

private fun MoodEntryEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("dateTime", dateTime.toEpochMilli())
    put("emotion", emotion.name)
    put("intensity", intensity)
    put("trigger", trigger)
    put("peopleInvolved", stringListJson(peopleInvolved))
    put("situation", situation)
    put("automaticThoughts", automaticThoughts)
    put("physicalSensations", physicalSensations)
    put("actionsTaken", actionsTaken)
    put("recoveryTimeMinutes", recoveryTimeMinutes)
    put("lessonsLearned", lessonsLearned)
    put("categoryId", categoryId)
}

private fun JSONObject.toMoodEntryEntity() = MoodEntryEntity(
    id = getString("id"),
    dateTime = Instant.ofEpochMilli(getLong("dateTime")),
    emotion = enumValueOf<Emotion>(getString("emotion")),
    intensity = getInt("intensity"),
    trigger = getString("trigger"),
    peopleInvolved = getJSONArray("peopleInvolved").toStringList(),
    situation = getString("situation"),
    automaticThoughts = getString("automaticThoughts"),
    physicalSensations = getString("physicalSensations"),
    actionsTaken = getString("actionsTaken"),
    recoveryTimeMinutes = if (isNull("recoveryTimeMinutes")) null else getInt("recoveryTimeMinutes"),
    lessonsLearned = getString("lessonsLearned"),
    categoryId = optStringOrNull("categoryId"),
    profileId = null,
)

private fun HabitEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("scheduleType", scheduleType.name)
    put("customDaysOfWeek", intListJson(customDaysOfWeek))
    put("difficulty", difficulty.name)
    put("importance", importance.name)
    put("categoryId", categoryId)
    put("isArchived", isArchived)
    put("createdAt", createdAt.toEpochMilli())
}

private fun JSONObject.toHabitEntity() = HabitEntity(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    scheduleType = enumValueOf<HabitScheduleType>(getString("scheduleType")),
    customDaysOfWeek = getJSONArray("customDaysOfWeek").toIntList(),
    difficulty = enumValueOf<HabitDifficulty>(getString("difficulty")),
    importance = enumValueOf<HabitImportance>(getString("importance")),
    categoryId = optStringOrNull("categoryId"),
    isArchived = getBoolean("isArchived"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = null,
)

private fun HabitCompletionEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("habitId", habitId)
    put("date", date.toEpochDay())
    put("completed", completed)
    put("note", note)
}

private fun JSONObject.toHabitCompletionEntity() = HabitCompletionEntity(
    id = getString("id"),
    habitId = getString("habitId"),
    date = LocalDate.ofEpochDay(getLong("date")),
    completed = getBoolean("completed"),
    note = getString("note"),
    profileId = null,
)

private fun GoalEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("horizon", horizon.name)
    put("targetDate", targetDate?.toEpochDay())
    put("status", status.name)
    put("linkedHabitIds", stringListJson(linkedHabitIds))
    put("linkedJournalIds", stringListJson(linkedJournalIds))
    put("categoryId", categoryId)
    put("createdAt", createdAt.toEpochMilli())
    put("completedAt", completedAt?.toEpochMilli())
}

private fun JSONObject.toGoalEntity() = GoalEntity(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    horizon = enumValueOf<GoalHorizon>(getString("horizon")),
    targetDate = if (isNull("targetDate")) null else LocalDate.ofEpochDay(getLong("targetDate")),
    status = enumValueOf<GoalStatus>(getString("status")),
    linkedHabitIds = getJSONArray("linkedHabitIds").toStringList(),
    linkedJournalIds = getJSONArray("linkedJournalIds").toStringList(),
    categoryId = optStringOrNull("categoryId"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    completedAt = if (isNull("completedAt")) null else Instant.ofEpochMilli(getLong("completedAt")),
    profileId = null,
)

private fun MentalToughnessEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("type", type.name)
    put("dateTime", dateTime.toEpochMilli())
    put("title", title)
    put("description", description)
    put("emotionBefore", emotionBefore)
    put("emotionAfter", emotionAfter)
    put("outcome", outcome)
    put("lessonLearned", lessonLearned)
}

private fun JSONObject.toMentalToughnessEntity() = MentalToughnessEntity(
    id = getString("id"),
    type = enumValueOf<MentalToughnessType>(getString("type")),
    dateTime = Instant.ofEpochMilli(getLong("dateTime")),
    title = getString("title"),
    description = getString("description"),
    emotionBefore = getString("emotionBefore"),
    emotionAfter = getString("emotionAfter"),
    outcome = getString("outcome"),
    lessonLearned = getString("lessonLearned"),
    profileId = null,
)

private fun SelfBeliefEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("dateTime", dateTime.toEpochMilli())
    put("whatHappened", whatHappened)
    put("storyTelling", storyTelling)
    put("evidenceFor", evidenceFor)
    put("evidenceAgainst", evidenceAgainst)
    put("friendAdvice", friendAdvice)
    put("strengthsThatRemain", strengthsThatRemain)
    put("nextSmallAction", nextSmallAction)
}

private fun JSONObject.toSelfBeliefEntity() = SelfBeliefEntity(
    id = getString("id"),
    dateTime = Instant.ofEpochMilli(getLong("dateTime")),
    whatHappened = getString("whatHappened"),
    storyTelling = getString("storyTelling"),
    evidenceFor = getString("evidenceFor"),
    evidenceAgainst = getString("evidenceAgainst"),
    friendAdvice = getString("friendAdvice"),
    strengthsThatRemain = getString("strengthsThatRemain"),
    nextSmallAction = getString("nextSmallAction"),
    profileId = null,
)

private fun ReflectionEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("templateType", templateType.name)
    put("dateTime", dateTime.toEpochMilli())
    put("title", title)
    put("answers", JSONObject(answers))
    put("tags", stringListJson(tags))
}

private fun JSONObject.toReflectionEntity(): ReflectionEntity {
    val answersJson = getJSONObject("answers")
    val answers = answersJson.keys().asSequence().associateWith { answersJson.getString(it) }
    return ReflectionEntity(
        id = getString("id"),
        templateType = enumValueOf<ReflectionTemplateType>(getString("templateType")),
        dateTime = Instant.ofEpochMilli(getLong("dateTime")),
        title = getString("title"),
        answers = answers,
        tags = getJSONArray("tags").toStringList(),
        profileId = null,
    )
}

private fun ProblemEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("possibleCauses", stringListJson(possibleCauses))
    put("attemptsMade", stringListJson(attemptsMade))
    put("whatWorked", whatWorked)
    put("whatFailed", whatFailed)
    put("status", status.name)
    put("notes", notes)
    put("createdAt", createdAt.toEpochMilli())
    put("updatedAt", updatedAt.toEpochMilli())
}

private fun JSONObject.toProblemEntity() = ProblemEntity(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    possibleCauses = getJSONArray("possibleCauses").toStringList(),
    attemptsMade = getJSONArray("attemptsMade").toStringList(),
    whatWorked = getString("whatWorked"),
    whatFailed = getString("whatFailed"),
    status = enumValueOf<ProblemStatus>(getString("status")),
    notes = getString("notes"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    updatedAt = Instant.ofEpochMilli(getLong("updatedAt")),
    profileId = null,
)

private fun TaskEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("date", date.toEpochDay())
    put("completed", completed)
    put("createdAt", createdAt.toEpochMilli())
    put("triggers", stringListJson(triggers))
    if (scheduledAt != null) put("scheduledAt", scheduledAt)
}

private fun JSONObject.toTaskEntity() = TaskEntity(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    date = LocalDate.ofEpochDay(getLong("date")),
    completed = getBoolean("completed"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = null,
    triggers = if (has("triggers")) getJSONArray("triggers").toStringList() else emptyList(),
    scheduledAt = if (has("scheduledAt") && !isNull("scheduledAt")) getLong("scheduledAt") else null,
)

private fun InspirationEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("type", type.name)
    put("text", text)
    put("author", author)
    put("imagePath", imagePath)
    put("sortOrder", sortOrder)
    put("createdAt", createdAt.toEpochMilli())
}

private fun JSONObject.toInspirationEntity() = InspirationEntity(
    id = getString("id"),
    type = enumValueOf<InspirationType>(getString("type")),
    text = getString("text"),
    author = getString("author"),
    imagePath = optStringOrNull("imagePath"),
    sortOrder = getInt("sortOrder"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = null,
)
