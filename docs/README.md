# LifeOS Android — Project Documentation

LifeOS is a personal life-management Android app built with Kotlin, Jetpack Compose, and Room.
It helps users track goals, habits, moods, journal entries, tasks, and more — all in a single place.

---

## Table of Contents

| Doc | What it covers |
|-----|---------------|
| [Architecture](architecture.md) | Tech stack, layer separation, DI, SOLID decisions |
| [Database](database.md) | Room schema, all entities, migrations history |
| [Navigation](navigation.md) | NavHost, routes, bottom nav, More sub-graph |
| [Features](features.md) | Index of all feature modules |
| [Profile System](profiles.md) | User profiles, demo profiles, profile switching |
| [Check-In System](checkin.md) | Scheduled follow-ups, commitments, auto-dialog |
| [Dashboard](dashboard.md) | Aggregated home screen, live reactive data |
| [Demo Mode](demo-mode.md) | Demo seeds, DemoTemplate, dynamic anchor dates |

---

## Quick Overview

```
app/
├── core/
│   ├── database/         Room AppDatabase + Converters
│   ├── demo/             Profile system: DemoModeRepository, DemoModeManager, DemoSeeder
│   ├── navigation/       LifeOSNavHost, routes, bottom/more nav items
│   └── ui/               Shared composables (LifeOSCard, LifeOSScaffold, LifeOSTopBar)
├── di/                   Hilt modules: DatabaseModule, DemoModule, RepositoryModule, AiModule
└── feature/
    ├── analytics/        Analytics screen
    ├── backup/           Import/Export feature
    ├── calendar/         Monthly calendar + day detail
    ├── category/         User-defined categories
    ├── checkin/          Check-in sessions + commitments
    ├── dashboard/        Aggregated home screen
    ├── demo/             Profiles UI (DemoModeScreen + DemoModeViewModel)
    ├── goal/             Goals (TODAY/MONTHLY/QUARTERLY/LONG_TERM)
    ├── habit/            Habits + daily completions
    ├── inspiration/      Quotes carousel
    ├── journal/          Journal entries
    ├── mentaltoughness/  Courage + difficult conversation tracker
    ├── mood/             Emotion + intensity tracker
    ├── problemsolver/    Open problem registry
    ├── profile/          ProfileEntity + ProfileDao (data layer for Profile)
    ├── reflection/       Structured reflection templates
    ├── search/           Cross-feature full-text search
    ├── selfbelief/       Self-belief reframing tool
    └── task/             Daily task list
```

---

## Key Design Principles

- **SOLID** — Single Responsibility per class; interfaces for every repository and manager
- **Reactive** — Room Flows + `combine()` as single source of truth; no manual refresh calls
- **Profile isolation** — every entity carries `profileId: String?` (`null` = user's own data)
- **Dependency Inversion** — feature modules depend on domain interfaces, not concrete impls

> See [Architecture](architecture.md) for the full breakdown.
