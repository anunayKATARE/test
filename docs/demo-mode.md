# Demo Mode

> Related: [Profile System](profiles.md) · [Architecture](architecture.md) · [Database](database.md)

Demo mode is a specialised use of the [Profile System](profiles.md). A demo profile is a `Profile` with `isDemo = true` and a `demoTemplate` pointing to a `DemoTemplate` enum value that defines what content gets seeded.

---

## DemoTemplate

```kotlin
enum class DemoTemplate(val id: String, val label: String, val description: String) {
    PERSONAL(id = "demo_personal", label = "Personal Demo", ...),
    WORK(id = "demo_work", label = "Work Demo", ...),
}
```

The ID (`demo_personal`, `demo_work`) is also the `profileId` stamped on all seeded entities. This means re-activating a template re-uses the same profile slot (after clearing old data).

---

## Seeder Architecture

### Interface

```kotlin
interface DemoSeeder {
    suspend fun seed(profileId: String, template: DemoTemplate, anchor: Instant)
    suspend fun clear(profileId: String)
}
```

### Dynamic Dates

`anchor` is captured as `Instant.now()` at the moment `activateDemoTemplate()` is called. All seeders use `anchor` instead of their own `Instant.now()` calls, so:

- All entries are consistently dated relative to activation time
- Re-activating a demo 2 weeks later shows data as "1 day ago", "2 days ago", etc. relative to the new activation
- Dates are testable and deterministic (no hidden time dependencies inside seeders)

### Multibinding Discovery

Seeders are discovered via Hilt multibinding. `DemoModeManagerImpl` receives `Set<DemoSeeder>`:

```kotlin
class DemoModeManagerImpl @Inject constructor(
    private val seeders: Set<@JvmSuppressWildcards DemoSeeder>,
    ...
)
```

Each seeder is registered in `DemoModule.kt`:
```kotlin
@Binds @IntoSet
abstract fun bindHabitDemoSeeder(impl: HabitDemoSeeder): DemoSeeder
```

Adding a new feature's demo data never requires modifying `DemoModeManagerImpl` or any existing seeder (**Open/Closed Principle**).

---

## Seeder Implementations

| Feature | Seeder | Content per template |
|---------|--------|---------------------|
| Category | `CategoryDemoSeeder` | WORK: Engineering/Product/Career · PERSONAL: Health/Relationships/Finance |
| Goal | `GoalDemoSeeder` | WORK: Q3 roadmap, AWS cert, mentoring · PERSONAL: 10K run, books, savings |
| Habit | `HabitDemoSeeder` | WORK: Deep work, Inbox zero, Standup · PERSONAL: Meditation, Water, Walk |
| Mood | `MoodDemoSeeder` | WORK: Anxiety/Pride/Frustration · PERSONAL: Joy/Gratitude/Calm |
| Journal | `JournalDemoSeeder` | 2 entries per template |
| Task | `TaskDemoSeeder` | WORK: PR review, sprint demo, 1:1 · PERSONAL: groceries, mom, gym |
| Inspiration | `InspirationDemoSeeder` | 2 quotes per template |
| Problem | `ProblemDemoSeeder` | WORK: Flaky CI · PERSONAL: Sleep schedule |
| Mental Toughness | `MentalToughnessDemoSeeder` | 1 entry per template |
| Self-Belief | `SelfBeliefDemoSeeder` | 1 entry per template |
| Reflection | `ReflectionDemoSeeder` | WORK: Weekly review · PERSONAL: Evening reflection |

---

## Activation Flow

```
User taps "Start Personal Demo"
    → DemoModeViewModel.activateDemoTemplate(DemoTemplate.PERSONAL)
        → DemoModeManagerImpl.activateDemoTemplate(template)
            1. anchor = Instant.now()
            2. seeder.clear("demo_personal") for all 11 seeders
            3. seeder.seed("demo_personal", PERSONAL, anchor) for all 11 seeders
            4. demoModeRepository.upsertProfile(profile)   // persists to Room
            5. demoModeRepository.setActiveProfile(profile) // DataStore → triggers flow
            6. tourState.start()                            // guided tour begins
    → All repository filterByActiveProfile() Flows emit with new profile
    → Dashboard, Goals, Habits, etc. immediately show demo data
    → DemoTourOverlay appears
```

---

## Deactivation Flow

```
User taps "Exit to My Data"
    → DemoModeManagerImpl.deactivate()
        1. active = demoModeRepository.activeProfile.first()
        2. if (active.isDemo) seeders.forEach { it.clear(active.id) }
        3. demoModeRepository.setActiveProfile(null)
        4. tourState.skipTour()
    → All repository Flows emit with profile = null
    → All screens show the user's own data (profileId = null entities)
```

---

## Guided Tour

After demo activation, `DemoTourState.start()` fires. `DemoTourOverlay` appears at the bottom of the screen and guides the user through key screens (Dashboard → Habits → Goals → Journal → More).

The tour is in-session only (not persisted beyond the current app process lifecycle).
