# Cloud Token Chat (Android)

A native Android app that lets you chat with an LLM of your choice using your own API key for that provider.

## How it works

- Open the app, tap the gear icon, pick a provider (Anthropic Claude, OpenAI ChatGPT, or Google Gemini), paste your API key for that provider, and pick a model.
- Each provider's key is stored separately in `EncryptedSharedPreferences` on-device (AES-256) — a key is never sent anywhere except directly to that provider's own API from your phone.
- Chat requests go straight from the app to the selected provider's API over HTTPS using OkHttp.

## Architecture: adding a new provider

The app is built around a single abstraction, `ChatProvider` (`app/src/main/java/com/cloudtokenchat/app/provider/ChatProvider.kt`), so the storage layer, `ChatViewModel`, and UI never depend on any specific vendor's request/response shape (Dependency Inversion). Each vendor gets its own class implementing `ChatProvider` (Single Responsibility), and all of them are interchangeable wherever a `ChatProvider` is expected (Liskov Substitution).

To add a new vendor:

1. Create a new class implementing `ChatProvider` in `app/src/main/java/com/cloudtokenchat/app/provider/`, following the existing `AnthropicProvider` / `OpenAiProvider` / `GeminiProvider` as templates — it owns its own endpoint URL, request body shape, auth header, and response parsing.
2. Add it to the list in `ProviderRegistry.kt`.

No changes to `ChatViewModel`, `TokenStore`, or `MainActivity` are needed (Open/Closed) — they all work purely in terms of `ChatProvider`, `ProviderRegistry`, and provider IDs.

## Project structure

- `app/src/main/java/com/cloudtokenchat/app/MainActivity.kt` — Jetpack Compose UI (chat screen + provider/model/key settings dialog)
- `app/src/main/java/com/cloudtokenchat/app/ChatViewModel.kt` — chat state and message history, talks only to `ChatProvider`
- `app/src/main/java/com/cloudtokenchat/app/TokenStore.kt` — encrypted, per-provider storage for API keys and chosen models
- `app/src/main/java/com/cloudtokenchat/app/provider/` — the provider abstraction:
  - `ChatProvider.kt` — the interface every vendor implements
  - `ProviderRegistry.kt` — catalog of available providers
  - `NetworkClient.kt` — shared OkHttp client
  - `AnthropicProvider.kt`, `OpenAiProvider.kt`, `GeminiProvider.kt` — vendor implementations

## Building locally

Open the project root in Android Studio (Jellyfish or newer) and let it sync, or from the command line with a Gradle 8.7+ install:

```bash
gradle assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Building in CI

The `.github/workflows/android-apk.yml` workflow builds a debug APK on every push and uploads it as a workflow artifact named `app-debug-apk`.
