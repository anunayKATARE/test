# Architecture

> Related: [Database](database.md) · [Navigation](navigation.md) · [Profile System](profiles.md)

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material3) |
| Architecture | MVVM + Repository pattern |
| DI | Hilt (Dagger) |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| Preferences | DataStore (Preferences) |
| Images | Coil |
| Navigation | Compose Navigation |

---

## Layer Separation

Each feature follows a strict three-layer architecture:

```
Presentation (ViewModel + Screen)
    ↓ depends on
Domain (Repository interface + domain models)
    ↓ depends on
Data (Room DAO + Entity + RepositoryImpl)
```

### Rules
- `domain/` contains only pure Kotlin: `data class`, `interface`, `enum class`
- `data/` contains Room entities, DAOs, `RepositoryImpl`; never imported by `presentation/`
- `presentation/` contains `ViewModel` and `@Composable`; never imports Room entities
- Cross-feature communication happens only through domain interfaces (DIP)

---

## Dependency Injection

Hilt provides all dependencies. Four modules:

| Module | What it provides |
|--------|-----------------|
| `DatabaseModule` | `AppDatabase`, all DAOs, all Room migrations |
| `DemoModule` | `DemoModeRepository`, `DemoModeManager`, all `DemoSeeder` multibindings, DataStore |
| `RepositoryModule` | All `@Binds` for feature repositories (Goal, Habit, Mood, etc.) |
| `AiModule` | `AiInsightProvider` (currently `NoOpAiInsightProvider`) |

Seeder discovery uses Hilt's **multibinding** (`@IntoSet`):

```kotlin
// DemoModule.kt
@Binds @IntoSet
abstract fun bindHabitDemoSeeder(impl: HabitDemoSeeder): DemoSeeder
```

`DemoModeManagerImpl` receives `Set<DemoSeeder>` and iterates all of them — adding a new feature's seeder never touches any existing code (Open/Closed Principle).

---

## Reactive Data Flow

All data flows are reactive: no `viewModel.refresh()` or one-shot suspend calls from the UI layer.

```kotlin
// DashboardViewModel.kt
val uiState: StateFlow<DashboardUiState> = combine(
    goalRepository.observeGoalsByHorizon(GoalHorizon.TODAY),
    todaysHabits,   // itself a combine of active habits + completions
    moodRepository.observeRecent(5),
    problemRepository.observeOpen(),
    heatmapState,
) { goals, habits, moods, problems, heatmap ->
    DashboardUiState(goals, habits, moods, problems, heatmap)
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState())
```

Write from any screen → Room invalidates relevant DAOs → all observing Flows emit → ViewModel recomputes → UI recomposed. Zero coupling between screens.

---

## Profile Isolation

Every Room entity carries a nullable `profileId: String?`:

- `null` → belongs to the user's own data (no profile active)
- `"demo_personal"` → belongs to the Personal Demo profile
- `"demo_work"` → belongs to the Work Demo profile
- Any UUID → belongs to a user-created profile

Every `RepositoryImpl` filters by the active profile using a private extension:

```kotlin
private fun Flow<List<XxxEntity>>.filterByActiveProfile() =
    combine(demoModeRepository.activeProfile) { entities, profile ->
        entities.filter { it.profileId == profile?.id }
    }
```

Switching profiles is a single DataStore write; Room Flows propagate the change automatically.

> See [Profile System](profiles.md) for the full design.

---

## SOLID Application

| Principle | Where applied |
|-----------|--------------|
| **S**RP | One ViewModel per screen; one Repository per feature domain |
| **O**CP | `DemoSeeder` multibinding: new features add a seeder without touching existing ones |
| **L**SP | All `RepositoryImpl` classes are drop-in replacements for their interfaces |
| **I**SP | Small, focused interfaces (`CheckInRepository` has 3 methods; `GoalRepository` has ~6) |
| **D**IP | `DashboardViewModel` depends on `GoalRepository` (interface), not `GoalRepositoryImpl` |
