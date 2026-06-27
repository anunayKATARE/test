# Cloud Token Chat (Android)

A native Android app that lets you chat with Claude using your own Anthropic API token.

## How it works

- Open the app, tap the gear icon, and paste your Anthropic API key plus pick a model.
- The token is stored in `EncryptedSharedPreferences` on-device (AES-256) — it is never sent anywhere except directly to `https://api.anthropic.com` from your phone.
- Chat requests go straight from the app to the Anthropic Messages API over HTTPS using OkHttp.

## Project structure

- `app/src/main/java/com/cloudtokenchat/app/MainActivity.kt` — Jetpack Compose UI (chat screen + settings dialog)
- `app/src/main/java/com/cloudtokenchat/app/ChatViewModel.kt` — chat state and message history
- `app/src/main/java/com/cloudtokenchat/app/AnthropicClient.kt` — OkHttp client that calls the Anthropic Messages API
- `app/src/main/java/com/cloudtokenchat/app/TokenStore.kt` — encrypted storage for the API token

## Building locally

Open the project root in Android Studio (Jellyfish or newer) and let it sync, or from the command line with a Gradle 8.7+ install:

```bash
gradle assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

## Building in CI

The `.github/workflows/android-apk.yml` workflow builds a debug APK on every push and uploads it as a workflow artifact named `app-debug-apk`.
