package com.lifeos.app.feature.backup.data

import androidx.room.withTransaction
import com.lifeos.app.core.database.AppDatabase
import com.lifeos.app.core.demo.DemoModeRepository
import com.lifeos.app.feature.backup.domain.BackupRepository
import com.lifeos.app.feature.category.data.CategoryDao
import com.lifeos.app.feature.category.data.CategoryEntity
import com.lifeos.app.feature.checkin.data.CheckInCommitmentEntity
import com.lifeos.app.feature.checkin.data.CheckInDao
import com.lifeos.app.feature.checkin.data.CheckInSessionEntity
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
import com.lifeos.app.feature.plan.data.DayPlanDao
import com.lifeos.app.feature.plan.data.DayPlanEntity
import com.lifeos.app.feature.problemsolver.data.ProblemDao
import com.lifeos.app.feature.problemsolver.data.ProblemEntity
import com.lifeos.app.feature.problemsolver.domain.ProblemStatus
import com.lifeos.app.feature.profile.data.ProfileDao
import com.lifeos.app.feature.profile.data.ProfileEntity
import com.lifeos.app.feature.reflection.data.ReflectionDao
import com.lifeos.app.feature.reflection.data.ReflectionEntity
import com.lifeos.app.feature.reflection.domain.ReflectionTemplateType
import com.lifeos.app.feature.selfbelief.data.SelfBeliefDao
import com.lifeos.app.feature.selfbelief.data.SelfBeliefEntity
import com.lifeos.app.feature.task.data.TaskAlarmRescheduler
import com.lifeos.app.feature.task.data.TaskDao
import com.lifeos.app.feature.task.data.TaskEntity
import com.lifeos.app.feature.timelog.data.TimeLogDao
import com.lifeos.app.feature.timelog.data.TimeLogEntity
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

private const val SCHEMA_VERSION = 2

class BackupRepositoryImpl @Inject constructor(
    private val database: AppDatabase,
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
    private val checkInDao: CheckInDao,
    private val profileDao: ProfileDao,
    private val dayPlanDao: DayPlanDao,
    private val timeLogDao: TimeLogDao,
    private val demoModeRepository: DemoModeRepository,
    private val alarmRescheduler: TaskAlarmRescheduler,
) : BackupRepository {

    override suspend fun exportSnapshot(): String {
        // Full snapshot: every row of every table, all profiles included,
        // with each row's profileId preserved
        val root = JSONObject()
        root.put("schemaVersion", SCHEMA_VERSION)
        root.put("exportedAt", Instant.now().toEpochMilli())
        root.put("profiles", JSONArray(profileDao.getAllForBackup().map { it.toJson() }))
        root.put("categories", JSONArray(categoryDao.getAllForBackup().map { it.toJson() }))
        root.put("journalEntries", JSONArray(journalDao.getAllForBackup().map { it.toJson() }))
        root.put("moodEntries", JSONArray(moodDao.getAllForBackup().map { it.toJson() }))
        root.put("habits", JSONArray(habitDao.getAllForBackupHabits().map { it.toJson() }))
        root.put("habitCompletions", JSONArray(habitDao.getAllForBackupCompletions().map { it.toJson() }))
        root.put("goals", JSONArray(goalDao.getAllForBackup().map { it.toJson() }))
        root.put("mentalToughnessEntries", JSONArray(mentalToughnessDao.getAllForBackup().map { it.toJson() }))
        root.put("selfBeliefReflections", JSONArray(selfBeliefDao.getAllForBackup().map { it.toJson() }))
        root.put("reflectionEntries", JSONArray(reflectionDao.getAllForBackup().map { it.toJson() }))
        root.put("problems", JSONArray(problemDao.getAllForBackup().map { it.toJson() }))
        root.put("tasks", JSONArray(taskDao.getAllForBackup().map { it.toJson() }))
        root.put("inspirationItems", JSONArray(inspirationDao.getAllForBackup().map { it.toJson() }))
        root.put("checkInSessions", JSONArray(checkInDao.getAllForBackupSessions().map { it.toJson() }))
        root.put("checkInCommitments", JSONArray(checkInDao.getAllForBackupCommitments().map { it.toJson() }))
        root.put("dayPlans", JSONArray(dayPlanDao.getAllForBackup().map { it.toJson() }))
        root.put("timeLogs", JSONArray(timeLogDao.getAllForBackup().map { it.toJson() }))
        return root.toString()
    }

    override suspend fun importSnapshot(json: String) {
        val root = JSONObject(json)
        if (root.optInt("schemaVersion", 1) >= 2) importV2(root) else importV1(root)
    }

    private suspend fun importV2(root: JSONObject) {
        // Parse the entire file FIRST — nothing is deleted unless every row parses
        val profiles = root.objects("profiles").map { it.toProfileEntity() }
        val categories = root.objects("categories").map { it.toCategoryEntity() }
        val journalEntries = root.objects("journalEntries").map { it.toJournalEntryEntity() }
        val moodEntries = root.objects("moodEntries").map { it.toMoodEntryEntity() }
        val habits = root.objects("habits").map { it.toHabitEntity() }
        val habitCompletions = root.objects("habitCompletions").map { it.toHabitCompletionEntity() }
        val goals = root.objects("goals").map { it.toGoalEntity() }
        val mentalToughness = root.objects("mentalToughnessEntries").map { it.toMentalToughnessEntity() }
        val selfBeliefs = root.objects("selfBeliefReflections").map { it.toSelfBeliefEntity() }
        val reflections = root.objects("reflectionEntries").map { it.toReflectionEntity() }
        val problems = root.objects("problems").map { it.toProblemEntity() }
        val tasks = root.objects("tasks").map { it.toTaskEntity() }
        val inspirations = root.objects("inspirationItems").map { it.toInspirationEntity() }
        val checkInSessions = root.objects("checkInSessions").map { it.toCheckInSessionEntity() }
        val checkInCommitments = root.objects("checkInCommitments").map { it.toCheckInCommitmentEntity() }
        val dayPlans = root.objects("dayPlans").map { it.toDayPlanEntity() }
        val timeLogs = root.objects("timeLogs").map { it.toTimeLogEntity() }

        database.withTransaction {
            profileDao.deleteAllForRestore()
            categoryDao.deleteAllForRestore()
            journalDao.deleteAllForRestore()
            moodDao.deleteAllForRestore()
            habitDao.deleteAllForRestoreHabits()
            habitDao.deleteAllForRestoreCompletions()
            goalDao.deleteAllForRestore()
            mentalToughnessDao.deleteAllForRestore()
            selfBeliefDao.deleteAllForRestore()
            reflectionDao.deleteAllForRestore()
            problemDao.deleteAllForRestore()
            taskDao.deleteAllForRestore()
            inspirationDao.deleteAllForRestore()
            checkInDao.deleteAllForRestoreSessions()
            checkInDao.deleteAllForRestoreCommitments()
            dayPlanDao.deleteAllForRestore()
            timeLogDao.deleteAllForRestore()

            profiles.forEach { profileDao.upsert(it) }
            categories.forEach { categoryDao.upsert(it) }
            journalEntries.forEach { journalDao.upsert(it) }
            moodEntries.forEach { moodDao.upsert(it) }
            habits.forEach { habitDao.upsertHabit(it) }
            habitCompletions.forEach { habitDao.upsertCompletion(it) }
            goals.forEach { goalDao.upsert(it) }
            mentalToughness.forEach { mentalToughnessDao.upsert(it) }
            selfBeliefs.forEach { selfBeliefDao.upsert(it) }
            reflections.forEach { reflectionDao.upsert(it) }
            problems.forEach { problemDao.upsert(it) }
            tasks.forEach { taskDao.upsertTask(it) }
            inspirations.forEach { inspirationDao.upsert(it) }
            checkInSessions.forEach { checkInDao.upsertSession(it) }
            if (checkInCommitments.isNotEmpty()) checkInDao.upsertCommitments(checkInCommitments)
            dayPlans.forEach { dayPlanDao.upsert(it) }
            timeLogs.forEach { timeLogDao.upsert(it) }
        }

        // Reset to the default profile so restored "My Data" rows are immediately
        // visible even when the backup came from another device
        demoModeRepository.setActiveProfile(null)

        // Restored tasks were written straight through the DAO — register their alarms
        alarmRescheduler.rescheduleAllFuture()
    }

    // Legacy (schema v1) files only ever contained profile-less rows; restore them
    // into the currently active profile like the original implementation did
    private suspend fun importV1(root: JSONObject) {
        val profileId = demoModeRepository.activeProfile.first()?.id
        database.withTransaction {
            categoryDao.deleteAllReal()
            root.objects("categories").forEach { categoryDao.upsert(it.toCategoryEntity().copy(profileId = profileId)) }

            journalDao.deleteAllReal()
            root.objects("journalEntries").forEach { journalDao.upsert(it.toJournalEntryEntity().copy(profileId = profileId)) }

            moodDao.deleteAllReal()
            root.objects("moodEntries").forEach { moodDao.upsert(it.toMoodEntryEntity().copy(profileId = profileId)) }

            habitDao.deleteAllRealHabits()
            root.objects("habits").forEach { habitDao.upsertHabit(it.toHabitEntity().copy(profileId = profileId)) }

            habitDao.deleteAllRealCompletions()
            root.objects("habitCompletions").forEach { habitDao.upsertCompletion(it.toHabitCompletionEntity().copy(profileId = profileId)) }

            goalDao.deleteAllReal()
            root.objects("goals").forEach { goalDao.upsert(it.toGoalEntity().copy(profileId = profileId)) }

            mentalToughnessDao.deleteAllReal()
            root.objects("mentalToughnessEntries").forEach { mentalToughnessDao.upsert(it.toMentalToughnessEntity().copy(profileId = profileId)) }

            selfBeliefDao.deleteAllReal()
            root.objects("selfBeliefReflections").forEach { selfBeliefDao.upsert(it.toSelfBeliefEntity().copy(profileId = profileId)) }

            reflectionDao.deleteAllReal()
            root.objects("reflectionEntries").forEach { reflectionDao.upsert(it.toReflectionEntity().copy(profileId = profileId)) }

            problemDao.deleteAllReal()
            root.objects("problems").forEach { problemDao.upsert(it.toProblemEntity().copy(profileId = profileId)) }

            taskDao.deleteAllReal()
            root.objects("tasks").forEach { taskDao.upsertTask(it.toTaskEntity().copy(profileId = profileId)) }

            inspirationDao.deleteAllReal()
            root.objects("inspirationItems").forEach { inspirationDao.upsert(it.toInspirationEntity().copy(profileId = profileId)) }
        }
        alarmRescheduler.rescheduleAllFuture()
    }
}

// Missing arrays are treated as empty so partial/older files never abort an import
private fun JSONObject.objects(name: String): List<JSONObject> {
    val arr = optJSONArray(name) ?: return emptyList()
    return (0 until arr.length()).map { arr.getJSONObject(it) }
}

private fun JSONArray.toStringList(): List<String> = (0 until length()).map { getString(it) }

private fun JSONArray.toIntList(): List<Int> = (0 until length()).map { getInt(it) }

private fun stringListJson(list: List<String>): JSONArray = JSONArray(list)

private fun intListJson(list: List<Int>): JSONArray = JSONArray(list)

private fun JSONObject.optStringOrNull(name: String): String? = if (isNull(name)) null else optString(name)

private fun ProfileEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("isDemo", isDemo)
    putOpt("demoTemplate", demoTemplate)
    put("createdAt", createdAt)
}

private fun JSONObject.toProfileEntity() = ProfileEntity(
    id = getString("id"),
    name = getString("name"),
    isDemo = getBoolean("isDemo"),
    demoTemplate = optStringOrNull("demoTemplate"),
    createdAt = getLong("createdAt"),
)

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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
)

private fun HabitCompletionEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("habitId", habitId)
    put("date", date.toEpochDay())
    put("completed", completed)
    put("note", note)
    putOpt("profileId", profileId)
}

private fun JSONObject.toHabitCompletionEntity() = HabitCompletionEntity(
    id = getString("id"),
    habitId = getString("habitId"),
    date = LocalDate.ofEpochDay(getLong("date")),
    completed = getBoolean("completed"),
    note = getString("note"),
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
)

private fun ReflectionEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("templateType", templateType.name)
    put("dateTime", dateTime.toEpochMilli())
    put("title", title)
    put("answers", JSONObject(answers))
    put("tags", stringListJson(tags))
    putOpt("profileId", profileId)
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
        profileId = optStringOrNull("profileId"),
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
    putOpt("profileId", profileId)
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
    profileId = optStringOrNull("profileId"),
)

private fun TaskEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("description", description)
    put("date", date.toEpochDay())
    put("completed", completed)
    put("createdAt", createdAt.toEpochMilli())
    put("triggers", stringListJson(triggers))
    putOpt("scheduledAt", scheduledAt)
    putOpt("scheduledEndAt", scheduledEndAt)
    put("isChore", isChore)
    putOpt("profileId", profileId)
}

private fun JSONObject.toTaskEntity() = TaskEntity(
    id = getString("id"),
    title = getString("title"),
    description = getString("description"),
    date = LocalDate.ofEpochDay(getLong("date")),
    completed = getBoolean("completed"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = optStringOrNull("profileId"),
    triggers = if (has("triggers")) getJSONArray("triggers").toStringList() else emptyList(),
    scheduledAt = if (has("scheduledAt") && !isNull("scheduledAt")) getLong("scheduledAt") else null,
    scheduledEndAt = if (has("scheduledEndAt") && !isNull("scheduledEndAt")) getLong("scheduledEndAt") else null,
    isChore = optBoolean("isChore", false),
)

private fun InspirationEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("type", type.name)
    put("text", text)
    put("author", author)
    put("imagePath", imagePath)
    put("sortOrder", sortOrder)
    put("createdAt", createdAt.toEpochMilli())
    putOpt("profileId", profileId)
}

private fun JSONObject.toInspirationEntity() = InspirationEntity(
    id = getString("id"),
    type = enumValueOf<InspirationType>(getString("type")),
    text = getString("text"),
    author = getString("author"),
    imagePath = optStringOrNull("imagePath"),
    sortOrder = getInt("sortOrder"),
    createdAt = Instant.ofEpochMilli(getLong("createdAt")),
    profileId = optStringOrNull("profileId"),
)

private fun CheckInSessionEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("scheduledAt", scheduledAt)
    putOpt("completedAt", completedAt)
    putOpt("profileId", profileId)
}

private fun JSONObject.toCheckInSessionEntity() = CheckInSessionEntity(
    id = getString("id"),
    scheduledAt = getLong("scheduledAt"),
    completedAt = if (isNull("completedAt")) null else getLong("completedAt"),
    profileId = optStringOrNull("profileId"),
)

private fun CheckInCommitmentEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("sessionId", sessionId)
    put("text", text)
    putOpt("linkedId", linkedId)
    putOpt("linkedType", linkedType)
    put("isCompleted", isCompleted)
    put("sortOrder", sortOrder)
}

private fun JSONObject.toCheckInCommitmentEntity() = CheckInCommitmentEntity(
    id = getString("id"),
    sessionId = getString("sessionId"),
    text = getString("text"),
    linkedId = optStringOrNull("linkedId"),
    linkedType = optStringOrNull("linkedType"),
    isCompleted = getBoolean("isCompleted"),
    sortOrder = getInt("sortOrder"),
)

private fun DayPlanEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("forDate", forDate.toEpochDay())
    put("plannedAt", plannedAt.toEpochMilli())
    put("selectedTaskIds", stringListJson(selectedTaskIds))
    put("intentions", intentions)
    putOpt("profileId", profileId)
}

private fun JSONObject.toDayPlanEntity() = DayPlanEntity(
    id = getString("id"),
    forDate = LocalDate.ofEpochDay(getLong("forDate")),
    plannedAt = Instant.ofEpochMilli(getLong("plannedAt")),
    selectedTaskIds = getJSONArray("selectedTaskIds").toStringList(),
    intentions = getString("intentions"),
    profileId = optStringOrNull("profileId"),
)

private fun TimeLogEntity.toJson() = JSONObject().apply {
    put("id", id)
    put("startedAt", startedAt.toEpochMilli())
    putOpt("endedAt", endedAt?.toEpochMilli())
    putOpt("linkedTaskId", linkedTaskId)
    putOpt("chore", chore)
    putOpt("profileId", profileId)
}

private fun JSONObject.toTimeLogEntity() = TimeLogEntity(
    id = getString("id"),
    startedAt = Instant.ofEpochMilli(getLong("startedAt")),
    endedAt = if (isNull("endedAt")) null else Instant.ofEpochMilli(getLong("endedAt")),
    linkedTaskId = optStringOrNull("linkedTaskId"),
    chore = optStringOrNull("chore"),
    profileId = optStringOrNull("profileId"),
)
