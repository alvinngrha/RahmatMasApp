# Repository Guidelines

## Project Structure & Module Organization
- `app/`: Android app module (Kotlin, Jetpack Compose)
  - Source: `app/src/main/java/`
  - Resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests
  - Unit: `app/src/test/`
  - Instrumentation/UI: `app/src/androidTest/`
- Supabase edge function: `supabase/functions/send-push/` (TypeScript)
- Build config: root `build.gradle.kts`, `settings.gradle.kts`, app config in `app/build.gradle.kts`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug`
- Install on device/emulator: `./gradlew installDebug`
- Run unit tests (JVM): `./gradlew testDebugUnitTest`
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest`
- Android Lint: `./gradlew lint`
- Launch activity after install: `adb shell am start -n com.example.rahmatmas/.MainActivity`

## Coding Style & Naming Conventions
- Kotlin + Compose + Material3; 4-space indent; Kotlin standard style.
- Architecture: immutable data, coroutines for async, `ViewModel` for UI state.
- Naming
  - UI: `...Screen` (e.g., `HomeCustomerScreen`)
  - State: `...ViewModel`, `...ViewModelFactory`
  - Data: `...Repository`, Room `...Dao` / `...Entity`
- Packages mirror features (e.g., `ui/admin/...`, `ui/customer/...`, `data/...`).

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation).
- Name tests to mirror class under test (e.g., `HomeCustomerViewModelTest`).
- Focus: ViewModel logic and repository interactions; UI tests cover critical flows.
- Commands: unit `./gradlew testDebugUnitTest`, instrumentation `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Conventional Commits: `feat: ...`, `fix: ...`, `refactor: ...`.
- PRs include: clear summary, linked issue (if any), test steps, and screenshots/GIFs for UI changes. Keep changes scoped and ensure CI builds green.

## Security & Configuration Tips
- SDK path via `local.properties`; do not commit keystores.
- Supabase keys/URLs live in `BuildConfig` for now; avoid committing sensitive production values. Prefer environment-specific or remote config for releases.
- Review diffs for secrets before pushing.

