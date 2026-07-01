# Features

> Related: [Architecture](architecture.md) · [Navigation](navigation.md)

---

## Feature Modules

| Feature | Package | Description |
|---------|---------|-------------|
| **Dashboard** | `feature/dashboard` | [Details →](dashboard.md) Aggregated home: goals, habits, mood, problems, heatmap |
| **Journal** | `feature/journal` | Rich journal entries with mood/goal links, photos, tags |
| **Mood** | `feature/mood` | Emotion + intensity tracking with triggers and recovery notes |
| **Habits** | `feature/habit` | Daily/weekly habits with completion tracking and heatmap |
| **Goals** | `feature/goal` | Horizons: TODAY, MONTHLY, QUARTERLY, LONG_TERM |
| **Tasks** | `feature/task` | Simple daily task list |
| **Calendar** | `feature/calendar` | Monthly calendar with day scores |
| **Day Detail** | `feature/calendar` | Per-day breakdown: tasks, habits, mood, journal |
| **Categories** | `feature/category` | User-defined coloured categories for entries |
| **Mental Toughness** | `feature/mentaltoughness` | Courage tracker + difficult conversation log |
| **Self-Belief** | `feature/selfbelief` | Cognitive reframing: evidence for vs against |
| **Reflection Library** | `feature/reflection` | Structured reflection templates (morning, evening, weekly) |
| **Problem Solver** | `feature/problemsolver` | Open problem registry with root-cause tracking |
| **Analytics** | `feature/analytics` | Cross-feature charts and statistics |
| **Search** | `feature/search` | Full-text search across journal, goals, habits, tasks |
| **Inspiration** | `feature/inspiration` | Quote carousel with optional image attachment |
| **Backup & Restore** | `feature/backup` | JSON export/import of the entire database |
| **Check-In** | `feature/checkin` | [Details →](checkin.md) Scheduled follow-ups with commitments |
| **Profiles** | `feature/demo` + `feature/profile` | [Details →](profiles.md) User profiles + demo mode |

---

## Shared Domain Concepts

### Profile isolation
Every feature's data is isolated by `profileId: String?`. Switching profiles instantly shows a completely different dataset. See [Profile System](profiles.md).

### Reactive flows
Every screen observes `StateFlow<UiState>` from its ViewModel. The ViewModel builds the state from `combine()` of live Room Flows. No screen ever calls a "refresh" function — data updates propagate automatically.

### Categories
Any entry (journal, mood, goal, habit, etc.) can be tagged with a user-defined `Category`. Categories are coloured and icons are simple string keys.

---

## Feature File Layout

Each feature follows the same layout:

```
feature/
└── xxx/
    ├── domain/
    │   ├── Xxx.kt                  data class (domain model)
    │   └── XxxRepository.kt        interface
    ├── data/
    │   ├── XxxEntity.kt            Room entity + toDomain() / toEntity()
    │   ├── XxxDao.kt               Room DAO
    │   ├── XxxRepositoryImpl.kt    implements XxxRepository
    │   └── XxxDemoSeeder.kt        implements DemoSeeder (if feature has demo data)
    └── presentation/
        ├── XxxViewModel.kt         HiltViewModel
        └── XxxScreen.kt            @Composable
```
