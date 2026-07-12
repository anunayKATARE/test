# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**LifeOS** is a single-module Android app (`:app`) for personal life management: tasks, habits, goals, journal, mood, calendar, reflections, time logging, and more. Kotlin + Jetpack Compose (Material3), MVVM + Repository pattern, Hilt DI, Room, DataStore, Coroutines/Flow. `minSdk 26`, `targetSdk/compileSdk 34`, JVM 17, Kotlin 2.0.21, AGP 8.6.1, KSP for Room/Hilt codegen.

Extensive prose documentation lives in `docs/` (architecture, database, navigation, features, profiles, check-in, dashboard, demo-mode). Read those for narrative detail; this file records the operational rules and the non-obvious traps.

## Commands

```bash
./gradlew assembleDebug          # build debug APK → app/build/outputs/apk/debug/
./gradlew test                   # JVM unit tests
./gradlew testDebugUnitTest --tests "com.lifeos.app.SomeTest"   # single test class
./gradlew connectedAndroidTest   # instrumented tests (needs device/emulator)
```

There is no lint/ktlint/detekt configuration — match surrounding code style by hand.

### CI / Delivery

`.github/workflows/build-apk.yml` builds a debug APK on **every push to any branch** and uploads it as artifact `lifeos-debug-apk`. The user installs these builds on a physical Samsung Galaxy (with a paired Galaxy Watch) — CI green + artifact is the delivery mechanism; there is no Play Store flow.

## Mandatory Workflow Rules

1. **Branch**: develop and push on `claude/lifeos-android-app-4q54n9`. Never push elsewhere without explicit permission.
2. **Version bump on every push**: increment `versionCode` and `versionName` in `app/build.gradle.kts` (`defaultConfig`) before each push — the user side-loads builds and relies on the version to confirm they're testing the new one.
3. **SOLID / system design**: the user repeatedly asks for SOLID adherence. Concretely: keep the 3-layer split below, put shared logic in a single injectable class instead of duplicating (see `TaskNotificationPoster`, `TaskAlarmRescheduler`), bind interfaces in Hilt modules, extend via new classes/multibindings rather than editing switch sites.

## Architecture

### Feature layout (strict 3 layers)

Every feature under `app/src/main/java/com/lifeos/app/feature/<name>/` follows:

```
domain/        pure Kotlin: data classes, enums, repository interfaces, use cases
data/          Room Entity + DAO + RepositoryImpl (+ mappers toDomain()/toEntity())
presentation/  @HiltViewModel + @Composable screen
```

- `presentation` never imports Room entities; `domain` has no Android imports.
- Repository interfaces are bound in `di/RepositoryModule.kt` (`@Binds @Singleton`). Other Hilt modules: `DatabaseModule` (AppDatabase + DAOs + migrations), `DemoModule` (profile system + `DemoSeeder` multibindings + the shared `DataStore<Preferences>`), `CalendarModule`, `AlarmModule`, `AiModule`, `NotificationPrefsModule`.
- All UI data flows are reactive: ViewModels expose `StateFlow` built with `combine(...)`/`flatMapLatest(...)` + `stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)`. No refresh methods except deliberately (diagnostics screen).
- Shared UI primitives: `core/ui/components/CommonComponents.kt` (`LifeOSCard`, `LifeOSScaffold`, `LifeOSTopBar(title, actions)`), `EmptyState`. Use them for any new screen.

### The profile system — biggest cross-cutting trap

Every Room entity (17 tables, `core/database/AppDatabase.kt`) carries `profileId: String?`. The active profile is resolved by `DemoModeRepositoryImpl`:

- **There is no null active profile in practice.** When DataStore has no selection, the active profile is a *synthetic* default with id `DemoModeRepository.DEFAULT_PROFILE_ID == "__default__"` ("My Data"). Rows created by users are therefore stamped `"__default__"`, **not** `NULL`.
- Every `RepositoryImpl` stamps writes with the active profile id and filters reads with `profileId == activeProfile.id` (via `filterByActiveProfile()`-style combines). **Any query or write that bypasses this makes data invisible.** This exact mistake broke backup v1 (`WHERE profileId IS NULL` matched nothing).
- DAO conventions per table: `deleteAllByProfile(profileId)` (profile wipe), `getAllReal()`/`deleteAllReal()` (legacy `IS NULL` rows — used only by backup v1 import), `getAllForBackup()`/`deleteAllForRestore()` (whole table, all profiles — backup v2). Multi-table DAOs suffix the names (`getAllForBackupHabits`/`...Completions`, `...Sessions`/`...Commitments`).
- Demo profiles are seeded by `DemoSeeder` implementations registered `@IntoSet` in `DemoModule`; `DemoModeManagerImpl` iterates the set. New feature ⇒ new seeder binding, never edit the manager.

### Room database

- `core/database/AppDatabase.kt`, currently **version 11**, `exportSchema = true` with schemas in `app/schemas/` (KSP arg `room.schemaLocation`).
- Migrations are provided in `di/DatabaseModule.kt`. When adding a column/table: bump the version, add a `Migration` there, keep entity defaults in sync with the SQL defaults.
- Converters (`core/database/Converters.kt`) handle `Instant`, `LocalDate`, `List<String>`, `List<Int>`, `Map<String,String>`.

### Task scheduling / notification pipeline (hard-won — do not regress)

Chain: `TaskViewModel.saveTask()` → `TaskRepositoryImpl.upsertTask()` → `TaskAlarmSchedulerImpl` (AlarmManager) → `TaskAlarmReceiver` (BroadcastReceiver) → `TaskNotificationPoster` → NotificationManager (+ Galaxy Watch via `NotificationCompat.WearableExtender`).

Rules baked in by painful debugging:

1. **Alarms are wiped by app update, force-stop, and reboot.** `TaskAlarmRescheduler.rescheduleAllFuture()` is the single re-registration point, invoked from: `TaskBootReceiver` (handles both `BOOT_COMPLETED` and `MY_PACKAGE_REPLACED`), `LifeOSApplication.onCreate` (every-launch safety net), and backup import (restored tasks bypass the scheduler). If you ever write tasks through the DAO directly, call the rescheduler afterwards.
2. `TaskRepositoryImpl.upsertTask` runs in `withContext(NonCancellable)` so a cancelled ViewModel scope can't persist the task but skip alarm registration. Keep it that way.
3. `TaskAlarmSchedulerImpl`: `setExactAndAllowWhileIdle`, with `setAndAllowWhileIdle` fallback when `canScheduleExactAlarms()` is false (API 31+). Instants in the past are silently skipped. PendingIntent identity = `taskId.hashCode()` + `FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE` (idempotent rescheduling).
4. Manifest permissions: `USE_EXACT_ALARM` (API 33+, auto-granted) plus `SCHEDULE_EXACT_ALARM` capped `maxSdkVersion="32"`. `POST_NOTIFICATIONS` is requested at runtime in `MainActivity.onStart`.
5. BroadcastReceivers do async work with `goAsync()` + `CoroutineScope(Dispatchers.IO)` and get dependencies through Hilt `@EntryPoint` interfaces (receivers can't use constructor injection). `TaskAlarmReceiver` falls back to posting directly on the default channel if the EntryPoint/preference read throws — a notification must never be silently dropped.
6. **Notification channels** are created in `LifeOSApplication.onCreate`, one per `SoundProfile` enum value (`feature/settings/domain/SoundProfile.kt`): `task_alarms` (sound+vibrate, HIGH), `task_alarms_vibrate` (vibrate only), `task_alarms_silent` (LOW). The user's choice is stored via `NotificationPrefsRepository` (DataStore) and read by `TaskNotificationPoster` at post time. Channel settings are immutable after creation on Android — changing behavior means adding a new channel id, not editing an existing one.
7. `feature/settings/presentation/NotificationSettingsScreen` is the diagnostics surface: sound-profile radio buttons, three status checks (notifications enabled, exact alarms allowed, battery optimization off — each with a fix button opening the right system settings screen), "Test now" (instant notification through the real poster), "Test alarm (1 min)" (full AlarmManager round-trip), and a "Scheduled reminders" list from `TaskRepository.getFutureScheduled()`. When debugging "notifications don't work" reports, point the user here first and extend this screen rather than guessing.

### Time-of-day text input convention

Users type times on a numeric keyboard with **no colon key**. `TaskViewModel` owns the convention:

- `autoFormatTime(input)`: strips non-digits, keeps max 4, inserts `:` after the 2nd digit as the user types (`"1430"` → `"14:30"`). Applied in every `update*TimeText` handler, including quick-plan.
- `parseTime(text, date)`: accepts `HH:mm`, 4-digit `HHmm`, 3-digit `Hmm` (→ `0H:mm`); returns `Instant?` in the system zone on `date`.
- **`saveTask()` re-parses the text fields at save time** — never rely on the IME Done action having fired; users tap "Create" with the keyboard still open.

### Backup / restore (schema v2)

`feature/backup/data/BackupRepositoryImpl.kt`:

- Export: `schemaVersion: 2`, every row of **all 17 tables across all profiles** (via `getAllForBackup*`), each row carrying `profileId` (`putOpt` — absent means null), plus the `profiles` table itself.
- Import v2: **parse the entire JSON before deleting anything**, then wipe + reinsert inside `database.withTransaction { }`; afterwards `setActiveProfile(null)` (falls back to `"__default__"`) and `alarmRescheduler.rescheduleAllFuture()`.
- Import v1 (legacy files): replaces only `IS NULL` rows, stamped with the active profile; missing arrays tolerated (`objects()` returns empty for absent keys).
- **When adding a field to any entity, update its `toJson()`/`toXxxEntity()` mapper pair here in the same commit** — Task's `scheduledEndAt`/`isChore` were once silently dropped this way. New tables must be added to export, importV2 (parse + wipe + insert), and get `getAllForBackup`/`deleteAllForRestore` DAO methods.

### Navigation

`core/navigation/`: `LifeOSDestinations.kt` holds `LifeOSRoutes` (string constants), `bottomNavItems` (Home, Journal, Mood, Habits, Goals, More), and `moreMenuSections` — the More tab is a 2-column grid of icon tiles grouped into sections **Plan & Do / Grow / Review / Settings** (`MoreScreen.kt`, `LazyVerticalGrid` with full-span headers). Adding a screen reachable from More requires: route constant, `MoreMenuItem(route, label, icon)` in the right section, and a `composable(...)` entry inside the `MORE_GRAPH` nested `navigation` block of `LifeOSNavHost.kt`. Global overlays (check-in dialog, plan reminder, time-log prompt, demo tour) are composed in `LifeOSNavHost` above the NavHost.

### Calendar integration

`feature/calendar/domain/CalendarRepository` (impl `AndroidCalendarRepository`) reads the device calendar (`READ_CALENDAR`, requested in `MainActivity` and inline on TaskScreen). `AvailabilityService.findFreeSlots(events, date)` powers free-slot chips. Busy visualisations render **two layers everywhere**: device calendar events (`errorContainer` color) and app-scheduled tasks (`primaryContainer`) — `DayBusyBar` in `TaskScreen.kt`, `DayTimelineSection` in `DayDetailScreen.kt`, and the tappable `VerticalDayTimeline` in the task form (tap sets start time, rounded to 30 min). Tasks without `scheduledEndAt` render as 30-minute blocks. Keep both layers when touching any timeline.

### Tasks domain specifics

`Task`: `id` (UUID string), `title`, `description`, `date: LocalDate` (the list/day bucket), `completed`, `createdAt`, `triggers: List<String>` (one-word failure triggers, lowercased), `scheduledAt`/`scheduledEndAt: Instant?` (reminder window), `isChore: Boolean`. Entity stores instants as epoch-millis `Long?`. The quick planner (calendar icon in Tasks top bar) lists up to 3 `observeUnscheduledUpcoming` tasks and schedules them in one confirm. Habit chips inside the task form pre-fill the title for scheduling a habit as a task.

## Gotchas Checklist

- Adding an entity field → Room migration + version bump in `DatabaseModule`, backup mapper pair, demo seeder if user-visible.
- Writing tasks via DAO directly → alarms won't exist; call `TaskAlarmRescheduler`.
- New periodic/exact alarm → route it through the existing scheduler/receiver pattern, never a new ad-hoc receiver without manifest registration + boot/update rescheduling.
- Any new DataStore preference → reuse the single shared `DataStore<Preferences>` provided by `DemoModule` (file `demo_mode_prefs`); do not create a second store file for small flags.
- BroadcastReceiver + Hilt → `@EntryPoint` + `goAsync()`; never block `onReceive`.
- Compose screens collect with `collectAsStateWithLifecycle()`.
- `versionCode`/`versionName` bump before every push (see Mandatory Workflow Rules).
