# Check-In System

> Related: [Architecture](architecture.md) · [Dashboard](dashboard.md) · [Database](database.md)

---

## Purpose

The check-in feature lets the user schedule a follow-up at a chosen time and commit to specific items (tasks, habits, goals, or free-text). When the scheduled time passes, a dialog opens automatically the next time the app is opened.

---

## Domain Models

```kotlin
// feature/checkin/domain/CheckInSession.kt
data class CheckInSession(
    val id: String,
    val scheduledAt: Instant,
    val commitments: List<CheckInCommitment> = emptyList(),
    val completedAt: Instant? = null,
) {
    val isOverdue: Boolean get() = completedAt == null && scheduledAt <= Instant.now()
    val isPending: Boolean get() = completedAt == null && !isOverdue
}

// feature/checkin/domain/CheckInCommitment.kt
enum class CommitmentType { TASK, HABIT, GOAL }

data class CheckInCommitment(
    val id: String,
    val text: String,
    val linkedId: String? = null,
    val linkedType: CommitmentType? = null,
    val isCompleted: Boolean = false,
)
```

---

## Repository

```kotlin
interface CheckInRepository {
    fun observeActive(): Flow<CheckInSession?>   // uncompleted session only
    suspend fun upsert(session: CheckInSession)
    suspend fun complete(id: String)
}
```

`observeActive()` returns the most recent uncompleted session, filtered by active profile.

---

## Database Tables

Two tables (added in DB migration v3 → v4):

- `check_in_sessions` — one row per session (`id`, `scheduledAt`, `completedAt`, `profileId`)
- `check_in_commitments` — one row per commitment per session; joined via `@Relation` in `CheckInSessionWithCommitments`

---

## Auto-Trigger Logic

In `LifeOSNavHost.kt`:

```kotlin
LaunchedEffect(checkInState.activeSession?.isOverdue) {
    if (checkInState.activeSession?.isOverdue == true) checkInViewModel.openDialog()
}
```

The dialog opens automatically whenever the app is foregrounded after the scheduled time passes — no push notifications required.

---

## CheckIn Dialog Flow

The dialog has two steps:

### Step 1: Review (shown when session is overdue)
- List of commitments with checkboxes
- "Done — schedule next" → advances to Schedule step
- "Skip" → marks session complete without scheduling a next one

### Step 2: Schedule
- **Time presets**: "In 1 hr", "In 3 hrs", "Tonight 9 PM", "Tomorrow 8 AM", "Custom…"
- **Custom picker**: `TimePicker` in a nested dialog; automatically advances to next day if the chosen time is already past
- **Commitments**: checkboxes from today's tasks, active habits, and active goals; plus a free-text field
- "Set check-in" (disabled until a time is chosen) → calls `onCompleteAndScheduleNext` or `onSchedule`

---

## ViewModel Actions

```kotlin
fun schedule(at: Instant, commitments: List<CheckInCommitment>)
fun completeAndScheduleNext(completedIds: Set<String>, nextAt: Instant?, nextCommitments: List<CheckInCommitment>)
fun skip()

// Commitment builders
fun buildCommitmentFromTask(task: Task): CheckInCommitment
fun buildCommitmentFromHabit(habit: Habit): CheckInCommitment
fun buildCommitmentFromGoal(goal: Goal): CheckInCommitment
fun buildFreeTextCommitment(text: String): CheckInCommitment
```

---

## Dashboard Integration

The Dashboard shows a `CheckInStatusCard` above the main content:

| State | Appearance |
|-------|-----------|
| No active session | Grey card, "Schedule a check-in" + Schedule button |
| Pending (future) | Grey card, scheduled time + commitment count + Edit button |
| Overdue | Error-color card with border, "Review now" button |

Tapping the card's action button opens the dialog directly.

---

## Profile Isolation

`CheckInRepositoryImpl` filters sessions by active profile:

```kotlin
combine(dao.observeActive(), demoModeRepository.activeProfile) { row, profile ->
    if (row == null || row.session.profileId != profile?.id) null else row.toDomain()
}
```
