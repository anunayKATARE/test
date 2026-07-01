# Database

> Related: [Architecture](architecture.md) · [Profile System](profiles.md) · [Check-In System](checkin.md)

Room database: `lifeos.db` · Current version: **5**

---

## Tables

### Core feature tables

| Table | Entity class | Key columns |
|-------|-------------|-------------|
| `categories` | `CategoryEntity` | id, name, colorHex, icon, orderIndex, isArchived, isHidden, createdAt, **profileId** |
| `journal_entries` | `JournalEntryEntity` | id, dateTime, title, body, tags, categoryId, moodEntryId, goalIds, habitIds, imageUris, **profileId** |
| `mood_entries` | `MoodEntryEntity` | id, dateTime, emotion, intensity, trigger, peopleInvolved, situation, automaticThoughts, lessonsLearned, **profileId** |
| `habits` | `HabitEntity` | id, title, scheduleType, customDaysOfWeek, difficulty, importance, isArchived, createdAt, **profileId** |
| `habit_completions` | `HabitCompletionEntity` | id, habitId, date (LocalDate as epoch day), completed, note, **profileId** |
| `goals` | `GoalEntity` | id, title, horizon, description, targetDate, status, linkedHabitIds, linkedJournalIds, createdAt, completedAt, **profileId** |
| `mental_toughness_entries` | `MentalToughnessEntity` | id, type, dateTime, title, emotionBefore, emotionAfter, **profileId** |
| `self_belief_reflections` | `SelfBeliefEntity` | id, dateTime, whatHappened, storyTelling, evidenceFor, evidenceAgainst, **profileId** |
| `reflection_entries` | `ReflectionEntity` | id, templateType, dateTime, title, answers (Map), **profileId** |
| `problems` | `ProblemEntity` | id, title, description, possibleCauses, attemptsMade, status, createdAt, updatedAt, **profileId** |
| `tasks` | `TaskEntity` | id, title, description, date (LocalDate), completed, createdAt, **profileId** |
| `inspiration_items` | `InspirationEntity` | id, type, text, author, imagePath, sortOrder, createdAt, **profileId** |

### System tables

| Table | Entity class | Key columns |
|-------|-------------|-------------|
| `check_in_sessions` | `CheckInSessionEntity` | id, scheduledAt (Long), completedAt (Long?), **profileId** |
| `check_in_commitments` | `CheckInCommitmentEntity` | id, sessionId, text, linkedId?, linkedType?, isCompleted, sortOrder |
| `profiles` | `ProfileEntity` | id, name, isDemo, demoTemplate?, createdAt |

---

## Type Converters (`Converters.kt`)

Room can't persist Kotlin collections or `java.time` types natively. `Converters.kt` handles:

| Kotlin type | Stored as |
|-------------|-----------|
| `Instant` | `Long` (epoch millis) |
| `LocalDate` | `Long` (epoch day) |
| `List<String>` | JSON string |
| `List<Enum>` | JSON string |
| `Map<String, String>` | JSON string |
| `Enum` | String name |

---

## Migration History

| Version | Change |
|---------|--------|
| 1 → 2 | Initial schema |
| 2 → 3 | Added `profileId TEXT` column to all 12 feature tables |
| 3 → 4 | Added `check_in_sessions` and `check_in_commitments` tables |
| 4 → 5 | Added `profiles` table for persisting user-created and demo profiles |

All migrations are non-destructive. The database also has `fallbackToDestructiveMigration()` as a safety net for development.

### v4 → v5 SQL
```sql
CREATE TABLE IF NOT EXISTS profiles (
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    isDemo INTEGER NOT NULL DEFAULT 0,
    demoTemplate TEXT,
    createdAt INTEGER NOT NULL
)
```

---

## Profile Isolation Pattern

Every feature table has `profileId TEXT` (nullable). A `null` value means the row belongs to the user's own data (no profile active).

All repository implementations filter by active profile using:

```kotlin
private fun Flow<List<XxxEntity>>.filterByActiveProfile(): Flow<List<XxxEntity>> =
    combine(demoModeRepository.activeProfile) { entities, profile ->
        entities.filter { it.profileId == profile?.id }
    }
```

And stamp new writes with the current profile:

```kotlin
val profileId = demoModeRepository.activeProfile.first()?.id
dao.upsert(entity.copy(profileId = profileId))
```

> See [Profile System](profiles.md) for the full profile lifecycle.

---

## AppDatabase Declaration

```kotlin
@Database(
    entities = [/* all 15 entity classes */],
    version = 5,
    exportSchema = true,   // schemas written to app/schemas/ (gitignored)
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { ... }
```
