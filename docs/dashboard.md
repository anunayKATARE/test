# Dashboard

> Related: [Architecture](architecture.md) · [Check-In System](checkin.md) · [Features](features.md)

---

## Purpose

The Dashboard is the home screen. It aggregates live data from all feature modules into a single scrollable view, giving the user a daily snapshot without navigating to each feature screen.

---

## Layout

From top to bottom:

1. **Inspiration Carousel** — rotates through the user's saved quotes
2. **Check-In Status Card** — shows the active follow-up session state ([details](checkin.md))
3. **Today's Goals** — goals with horizon `TODAY`
4. **Today's Habits** — habits scheduled for today, with toggle completion
5. **Recent Mood** — last 5 mood entries
6. **Open Problems** — active unsolved problems
7. **Habit Heatmap** — 7-week completion calendar with month navigation

Each subsection (except the carousel and check-in card) is wrapped in a `DashboardSection<T>` composable backed by `LifeOSCard`.

---

## Reactive Data Flow

`DashboardViewModel` builds `uiState` from a single `combine()`:

```kotlin
val uiState: StateFlow<DashboardUiState> = combine(
    goalRepository.observeGoalsByHorizon(GoalHorizon.TODAY),
    todaysHabits,                           // combine(activeHabits, completions)
    moodRepository.observeRecent(5),
    problemRepository.observeOpen(),
    heatmapState,                           // flatMapLatest on heatmapMonth
) { goals, habits, moods, problems, heatmap ->
    DashboardUiState(goals, habits, moods, problems, heatmap)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
```

Any write to any of these repositories (from any screen) immediately propagates to the Dashboard — no `refresh()` calls.

---

## Habit Filtering

`todaysHabits` filters habits that are scheduled for today:

```kotlin
private val todaysHabits: Flow<List<Pair<Habit, Boolean>>> = combine(
    habitRepository.observeActiveHabits(),
    habitRepository.observeCompletionsOn(LocalDate.now()),
) { habits, completions ->
    val today = LocalDate.now()
    val completionMap = completions.associateBy { it.habitId }
    habits
        .filter { habit ->
            when (habit.scheduleType) {
                HabitScheduleType.DAILY -> true
                HabitScheduleType.WEEKLY -> today.dayOfWeek in habit.customDaysOfWeek
                HabitScheduleType.CUSTOM -> today.dayOfWeek in habit.customDaysOfWeek
            }
        }
        .map { it to (completionMap[it.id]?.completed == true) }
}
```

---

## Habit Heatmap

The heatmap shows a 7-week grid of habit completions for the displayed month. Month navigation uses `flatMapLatest`:

```kotlin
private val heatmapMonth = MutableStateFlow(YearMonth.now())

private val heatmapState: Flow<HeatmapData> = heatmapMonth.flatMapLatest { month ->
    flow {
        val (start, end) = month.atDay(1) to month.atEndOfMonth()
        val counts = habitRepository.getCompletionCountByDay(start, end)
        val maxCount = habitRepository.getActiveHabitCount()
        emit(HeatmapData(month, counts, maxCount))
    }
}

fun nextMonth() { heatmapMonth.value = heatmapMonth.value.plusMonths(1) }
fun previousMonth() { heatmapMonth.value = heatmapMonth.value.minusMonths(1) }
```

---

## DashboardSection Composable

Each data section uses a private `DashboardSection<T>` composable that handles empty state automatically:

```kotlin
@Composable
private fun <T> DashboardSection(
    title: String,
    items: List<T>,
    emptyText: String,
    itemContent: @Composable (T, Boolean) -> Unit,   // item, isLast
)
```

If `items.isEmpty()` it shows the `emptyText` string. Otherwise it renders items separated by `HorizontalDivider`s, all inside a `LifeOSCard`.

---

## Habit Toggle

Tapping a habit row calls `viewModel.toggleHabit(habitId, date, currentlyCompleted)` which dispatches to `CompleteHabitUseCase` or `UncompleteHabitUseCase`. Room's Flow invalidation ensures the toggle reflects immediately.

---

## Dependencies

`DashboardViewModel` injects:

- `CompleteHabitUseCase`, `UncompleteHabitUseCase`
- `GoalRepository`, `HabitRepository`, `MoodRepository`, `JournalRepository`, `ProblemRepository`

`DashboardScreen` also receives a `CheckInViewModel` (via `hiltViewModel()`) to show the status card.
