# Navigation

> Related: [Architecture](architecture.md) · [Features](features.md)

Navigation is implemented with Jetpack Compose Navigation in `LifeOSNavHost.kt`.

---

## Bottom Navigation Bar

Five primary destinations:

| Label | Route | Screen |
|-------|-------|--------|
| Home | `dashboard` | `DashboardScreen` |
| Journal | `journal` | `JournalScreen` |
| Mood | `mood` | `MoodScreen` |
| Habits | `habits` | `HabitScreen` |
| Goals | `goals` | `GoalScreen` |
| More | `more_graph` | Sub-graph → `MoreScreen` |

---

## More Sub-Graph

Secondary destinations accessed via the More tab:

| Menu Label | Route | Screen |
|-----------|-------|--------|
| Calendar | `calendar` | `CalendarScreen` |
| Categories | `categories` | `CategoryScreen` |
| Mental Toughness | `mental_toughness` | `MentalToughnessScreen` |
| Self-Belief | `self_belief` | `SelfBeliefScreen` |
| Reflection Library | `reflection` | `ReflectionScreen` |
| Problem Solver | `problem_solver` | `ProblemScreen` |
| Analytics | `analytics` | `AnalyticsScreen` |
| Search | `search` | `SearchScreen` |
| Profiles | `demo_mode` | `DemoModeScreen` (now Profiles screen) |
| Backup & Restore | `backup` | `BackupScreen` |

> `DEMO_MODE` route constant was kept for backward compatibility; the label now reads "Profiles".

---

## Nested Routes

```
day_detail/{date}   →  DayDetailScreen
                         ↑ navigated from DashboardScreen and CalendarScreen
```

---

## Global Overlays

Two overlays are rendered on top of the entire nav graph in `LifeOSNavHost`:

1. **Check-in Dialog** (`CheckInDialog`) — auto-triggered when `activeSession.isOverdue == true`
2. **Demo Tour Overlay** (`DemoTourOverlay`) — step-by-step guided tour after demo activation

Both are placed in the `Box` wrapping the `NavHost` so they appear above all screens.

---

## Routes Object

```kotlin
object LifeOSRoutes {
    const val DASHBOARD = "dashboard"
    const val JOURNAL = "journal"
    const val MOOD = "mood"
    const val HABITS = "habits"
    const val GOALS = "goals"
    const val MORE = "more"
    const val MORE_GRAPH = "more_graph"
    const val CALENDAR = "calendar"
    const val DAY_DETAIL = "day_detail/{date}"
    const val CATEGORIES = "categories"
    const val MENTAL_TOUGHNESS = "mental_toughness"
    const val SELF_BELIEF = "self_belief"
    const val REFLECTION = "reflection"
    const val PROBLEM_SOLVER = "problem_solver"
    const val ANALYTICS = "analytics"
    const val SEARCH = "search"
    const val DEMO_MODE = "demo_mode"   // Profiles screen
    const val BACKUP = "backup"

    fun dayDetail(date: LocalDate): String = "day_detail/$date"
}
```

---

## Navigation State & SaveState

All bottom-nav and More items use:

```kotlin
navController.navigate(route) {
    popUpTo(graph.findStartDestination().id) { saveState = true }
    launchSingleTop = true
    restoreState = true
}
```

This means navigating between bottom-nav tabs preserves each tab's back stack and scroll position.
