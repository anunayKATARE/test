# Flappy Bird (Android)

A basic Flappy Bird clone for Android, written in Kotlin with a layered
architecture and SOLID principles applied throughout.

## Architecture

```
domain/            <- Pure Kotlin, no Android imports
  GameContracts.kt   Updatable / Collidable / Renderable / GameEntity (ISP)
  GameConfig.kt       All tuning constants in one place (OCP)
  entity/
    Bird.kt           Player entity; delegates gravity math to PhysicsEngine (DIP)
    Pipe.kt           Obstacle entity; same GameEntity contract as Bird (LSP)
  physics/
    PhysicsEngine.kt          Gravity model abstraction + StandardPhysicsEngine
    CollisionDetector.kt      AABB collision strategy abstraction
    ReachabilityCalculator.kt Max climb/fall in a given time, used to keep
                               procedurally placed pipes physically reachable

engine/             <- Game rules, still no Android imports
  GameEngine.kt       Orchestrates state machine + per-frame update; depends
                       only on interfaces (CollisionDetector, PipeSpawner,
                       ScoreTracker) passed into its constructor (DIP)
  PipeSpawner.kt      Obstacle-spawning strategy abstraction (OCP); the random
                       implementation constrains each new gap center to what
                       ReachabilityCalculator says is achievable from the
                       previous one, so no pipe ever requires more climb than
                       the bird's jump strength + max human tap rate allow
  ScoreTracker.kt     Scoring abstraction, swappable storage later

ui/                 <- The only layer allowed to import android.*
  GameRenderer.kt       Rendering abstraction
  CanvasGameRenderer.kt Draws bird/pipes/score onto an android.graphics.Canvas
  GameView.kt           Drives the frame loop (Choreographer), routes touch
                         input to GameEngine.onTap(), and is the composition
                         root that wires concrete implementations together

MainActivity.kt     <- Hosts GameView full-screen
```

### SOLID mapping

- **S**ingle Responsibility — physics, collision, spawning, scoring, and
  rendering are each their own class; `GameEngine` only coordinates them.
- **O**pen/Closed — new pipe-spawning patterns, physics models, or renderers
  can be added as new classes implementing existing interfaces without
  touching `GameEngine`.
- **L**iskov Substitution — `Bird` and `Pipe` both satisfy `GameEntity` and
  can be treated interchangeably wherever that contract is expected.
- **I**nterface Segregation — `Updatable`, `Collidable`, and `Renderable` are
  separate, minimal interfaces instead of one large one.
- **D**ependency Inversion — `GameEngine` and `Bird` depend on abstractions
  (`PhysicsEngine`, `CollisionDetector`, `PipeSpawner`, `ScoreTracker`)
  injected via constructors; concrete wiring only happens in `GameView`
  (the composition root).

## Building the APK

This repository's sandbox has no Android SDK and no network access to
Google's servers, so the APK is built by CI instead:

- Every push to a `claude/**` or `main` branch triggers
  `.github/workflows/android-build.yml`, which runs
  `./gradlew assembleDebug` on a GitHub-hosted runner (which has the
  Android SDK preinstalled) and uploads `app-debug.apk` as a build
  artifact.
- To build locally with Android Studio or a machine that has the Android
  SDK: `./gradlew assembleDebug` — output lands in
  `app/build/outputs/apk/debug/app-debug.apk`.

## Controls

Tap anywhere to start, flap, or restart after a game over.
