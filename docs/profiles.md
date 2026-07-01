# Profile System

> Related: [Architecture](architecture.md) · [Database](database.md) · [Demo Mode](demo-mode.md)

---

## Concept

A **Profile** is a named data context. All entries (goals, habits, moods, etc.) belong to exactly one profile. The active profile determines which data the user sees.

- `null` active profile → user's own real data (`profileId = null` on entities)
- User-created profile → custom named context (e.g., "Work", "Self Growth")
- Demo profile → pre-seeded sample data, marked `isDemo = true`

Switching profiles is instant — it's a single DataStore write; Room Flows propagate the change automatically.

---

## Domain Model

```kotlin
// core/demo/Profile.kt
data class Profile(
    val id: String,
    val name: String,
    val isDemo: Boolean,
    val demoTemplate: DemoTemplate? = null,   // set only for demo profiles
    val createdAt: Instant,
)
```

---

## DemoTemplate

Pre-defined demo content sets:

```kotlin
// core/demo/DemoTemplate.kt
enum class DemoTemplate(val id: String, val label: String, val description: String) {
    PERSONAL(id = "demo_personal", label = "Personal Demo", ...),
    WORK(id = "demo_work", label = "Work Demo", ...),
}
```

Demo profiles use fixed IDs (`demo_personal`, `demo_work`) so existing seeded data in the DB is reused across sessions.

---

## Repository Interface

```kotlin
// core/demo/DemoModeRepository.kt
interface DemoModeRepository {
    val activeProfile: Flow<Profile?>        // null = own data
    val allProfiles: Flow<List<Profile>>     // all persisted profiles
    suspend fun setActiveProfile(profile: Profile?)
    suspend fun upsertProfile(profile: Profile)
    suspend fun createUserProfile(name: String): Profile
    suspend fun deleteProfile(id: String)
}
```

### Implementation (`DemoModeRepositoryImpl`)

- **Active profile ID** stored in DataStore (`active_demo_profile_id`)
- **Profile list** stored in Room `profiles` table via `ProfileDao`
- `activeProfile` is a `combine(dao.observeAll(), activeProfileIdFlow)` — reactive with no polling
- Backward compat: if DataStore has a known demo template ID but no DB row, synthesises a `Profile` from `DemoTemplate.fromId()`

---

## Manager Interface

```kotlin
// core/demo/DemoModeManager.kt
interface DemoModeManager {
    suspend fun activateDemoTemplate(template: DemoTemplate): Profile
    suspend fun activateProfile(profile: Profile)
    suspend fun createUserProfile(name: String): Profile
    suspend fun deleteProfile(profileId: String)
    suspend fun deactivate()
}
```

### `activateDemoTemplate` flow

1. Captures `anchor = Instant.now()`
2. Creates a `Profile(id = template.id, isDemo = true, createdAt = anchor)`
3. Calls `seeder.clear(profileId)` on all seeders (removes stale demo data)
4. Calls `seeder.seed(profileId, template, anchor)` on all seeders (inserts fresh data)
5. Upserts the Profile record in Room
6. Sets active profile in DataStore
7. Starts the guided tour

All dates in seeded data are relative to `anchor`, so re-activating a demo always shows fresh-looking data.

---

## Profiles UI

**Screen**: `DemoModeScreen.kt` (route: `demo_mode`, label: "Profiles")

Three sections:

1. **Active Profile Banner** — shows which profile is active; "Exit to My Data" button when a profile is active
2. **Your Profiles** — user-created profiles with Switch/Delete actions; "+ Add Profile" button opens a dialog
3. **Demo Profiles** — one card per `DemoTemplate` with "Start Demo" / "Exit Demo" button

**ViewModel**: `DemoModeViewModel` exposes `ProfilesUiState`:
```kotlin
data class ProfilesUiState(
    val activeProfile: Profile?,
    val userProfiles: List<Profile>,      // non-demo profiles from DB
    val showCreateDialog: Boolean,
)
```

---

## Data Layer

```
feature/profile/data/
├── ProfileEntity.kt   @Entity(tableName = "profiles")
├── ProfileDao.kt      observeAll(), upsert(), deleteById(), getById()
```

`ProfileEntity` maps to/from `Profile` domain model via `toDomain()` / `toEntity()` extension functions.

---

## Profile Isolation in Feature Repos

Every feature `RepositoryImpl` receives `DemoModeRepository` via constructor injection and applies:

```kotlin
private fun Flow<List<XxxEntity>>.filterByActiveProfile(): Flow<List<XxxEntity>> =
    combine(demoModeRepository.activeProfile) { entities, profile ->
        entities.filter { it.profileId == profile?.id }
    }
```

This is the only place profile-awareness lives in each repository — no business logic scattered elsewhere.

---

## Adding a New Profile Template

1. Add an entry to `DemoTemplate` enum
2. Implement `DemoSeeder` in the relevant feature's `data/` package
3. Add `@Binds @IntoSet` for the new seeder in `DemoModule`
4. Done — the Profiles UI will show the new template automatically

No changes to `DemoModeManagerImpl`, `DemoModeScreen`, or any repository.
