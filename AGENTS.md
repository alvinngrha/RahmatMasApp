# Repository Guidelines

This guide explains how to build, test, and contribute to this Android + Supabase project. Keep changes focused, follow the conventions below, and verify locally before opening a PR.

## Project Structure & Module Organization
- Android app: `app/`
  - Source: `app/src/main/java/`, resources: `app/src/main/res/`
  - Firebase config (debug): `app/src/google-services.json`
- Tests: unit `app/src/test/`, instrumentation/UI `app/src/androidTest/`
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app `app/build.gradle.kts`
- Package by feature: `ui/admin/...`, `ui/customer/...`, `data/...`

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`
- Install on device/emulator: `./gradlew installDebug`
- Launch main activity: `adb shell am start -n com.example.rahmatmas/.MainActivity`
- Run unit tests: `./gradlew testDebugUnitTest`
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest` (device/emulator required)
- Static analysis: `./gradlew lint`

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose (Material 3); 4‑space indent; Kotlin official style.
- MVVM; one `ViewModel` per screen; immutable UI state; coroutines for side effects.
- Naming: screens `*Screen`; state `*ViewModel`/`*ViewModelFactory`; data `*Repository`; Room `*Dao`/`*Entity`.
- Keep composables small and previewable; avoid side effects in composables.

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation/UI).
- Test names mirror targets (e.g., `HomeCustomerViewModelTest`).
- Keep tests deterministic; use fakes/mocks; avoid network in unit tests.
- Run unit: `./gradlew testDebugUnitTest`; instrumentation: `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Use Conventional Commits: `feat:`, `fix:`, `refactor:`, `chore:`.
- PRs include a concise summary, linked issue, and test steps; add screenshots/GIFs for UI changes.
- Keep scope narrow; ensure build, tests, and lint are green before review.
- Reference relevant modules/files (e.g., `app/src/main/...`, `supabase/functions/send-push/...`).

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores.
- Store Supabase URL/keys via `BuildConfig`; no production secrets in VCS.
- Prefer environment-specific or remote config; review diffs for secrets.
- Validate `google-services.json` is non-production for debug builds.

## Architecture Overview
- MVVM with repositories and Room under `data/`.
- Compose UI organized by feature packages; unidirectional data flow.
- Coroutines for side effects; expose immutable state to UI.

