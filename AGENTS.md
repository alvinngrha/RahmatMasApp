# Repository Guidelines

This guide helps contributors build, test, and ship confidently in this Android + Supabase project.

## Project Structure & Module Organization
- Android app: `app/` — sources `app/src/main/java/`, resources `app/src/main/res/`, debug Firebase `app/src/google-services.json`.
- Tests: unit `app/src/test/`, instrumentation/UI `app/src/androidTest/`.
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`.
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app module `app/build.gradle.kts`.
- Package by feature: `ui/admin/...`, `ui/customer/...`, `data/...`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → output in `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Launch main activity: `adb shell am start -n com.example.rahmatmas/.MainActivity`.
- Run unit tests: `./gradlew testDebugUnitTest`.
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest` (device/emulator required).
- Static analysis: `./gradlew lint`.

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose (Material 3); 4‑space indent; follow official Kotlin style.
- MVVM: one `ViewModel` per screen; immutable UI state; coroutines for side effects.
- Names: screens `*Screen`; `*ViewModel`/`*ViewModelFactory`; data `*Repository`; Room `*Dao`/`*Entity`.
- Composables: keep small and previewable; avoid side effects inside composables.

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation/UI).
- Mirror targets in names (e.g., `HomeCustomerViewModelTest`).
- Deterministic tests; prefer fakes/mocks; avoid network in unit tests.
- Quick runs: `./gradlew testDebugUnitTest`, `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Conventional Commits: `feat:`, `fix:`, `refactor:`, `chore:`.
- PRs include summary, linked issue, test steps, and screenshots/GIFs for UI changes.
- Keep scope narrow; ensure build, tests, and lint are green; reference impacted paths (e.g., `app/src/main/...`, `supabase/functions/send-push/...`).

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores.
- Store Supabase URL/keys via `BuildConfig`; no production secrets in VCS.
- Use environment‑specific or remote config; ensure `google-services.json` is non‑production for debug builds.

## Architecture Overview
- MVVM with repositories and Room under `data/`.
- Compose UI by feature with unidirectional data flow.
- Coroutines handle side effects; expose immutable state to UI.

## Agent‑Specific Instructions
- Follow this file for code style, structure, and naming.
- Keep patches minimal and focused; avoid unrelated changes.
- Never add secrets or credentials to the repo.

