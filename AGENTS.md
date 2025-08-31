# Repository Guidelines

## Project Structure & Module Organization
- Android app module in `app/`.
  - Source: `app/src/main/java/`
  - Resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests: unit in `app/src/test/`, instrumentation/UI in `app/src/androidTest/`.
- Supabase edge function (TypeScript): `supabase/functions/send-push/`.
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app config in `app/build.gradle.kts`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` — produces `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Run unit tests (JVM): `./gradlew testDebugUnitTest`.
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest` (requires device/emulator).
- Android Lint: `./gradlew lint`.
- Launch main activity after install: `adb shell am start -n com.example.rahmatmas/.MainActivity`.

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose + Material3; 4‑space indent; Kotlin standard style.
- Architecture: immutable data, coroutines for async, `ViewModel` owns UI state.
- Naming: screens end with `...Screen`; state classes `...ViewModel` and `...ViewModelFactory`; data layer `...Repository`, Room `...Dao` / `...Entity`.
- Packages mirror features (e.g., `ui/admin/...`, `ui/customer/...`, `data/...`).

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation).
- Name tests to mirror the class under test (e.g., `HomeCustomerViewModelTest`).
- Run: unit `./gradlew testDebugUnitTest`; instrumentation `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Use Conventional Commits (e.g., `feat:`, `fix:`, `refactor:`).
- PRs include a clear summary, linked issue (if any), test steps, and screenshots/GIFs for UI changes.
- Keep changes scoped; ensure CI builds green before requesting review.

## Security & Configuration Tips
- Android SDK path via `local.properties`; never commit keystores.
- Supabase keys/URLs live in `BuildConfig` for now; avoid committing sensitive production values. Prefer env‑specific or remote config for releases.
- Review diffs for secrets before pushing.

## Architecture Overview
- MVVM with repositories; Compose UI in feature‑scoped packages; Room entities/DAOs in `data/`. Keep state immutable and side‑effects in coroutines.

