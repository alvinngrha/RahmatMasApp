# Repository Guidelines

## Project Structure & Module Organization
- Android app: `app/`
  - Source: `app/src/main/java/`
  - Resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests: unit in `app/src/test/`, instrumentation/UI in `app/src/androidTest/`.
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`.
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app config in `app/build.gradle.kts`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → outputs `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Launch main activity: `adb shell am start -n com.example.rahmatmas/.MainActivity`.
- Run unit tests (JVM): `./gradlew testDebugUnitTest`.
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest` (device/emulator required).
- Static analysis: `./gradlew lint`.

## Coding Style & Naming Conventions
- Language: Kotlin + Jetpack Compose + Material3; 4‑space indent; Kotlin standard style.
- Architecture: MVVM; each screen owns state via a `ViewModel` and immutable UI state.
- Naming: screens `...Screen`; state `...ViewModel`, `...ViewModelFactory`; data `...Repository`; Room `...Dao` / `...Entity`.
- Packages mirror features, e.g., `ui/admin/...`, `ui/customer/...`, `data/...`.

## Testing Guidelines
- Frameworks: JUnit for unit; AndroidX + Compose testing for instrumentation/UI.
- Test naming mirrors class under test, e.g., `HomeCustomerViewModelTest`.
- Run: unit via `./gradlew testDebugUnitTest`; instrumentation via `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Commits: Conventional Commits (`feat:`, `fix:`, `refactor:`, etc.).
- PRs: clear summary, linked issue, test steps, and screenshots/GIFs for UI changes.
- Scope changes narrowly; ensure CI/build is green before review.

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores.
- Store Supabase URL/keys in `BuildConfig` for now; avoid committing sensitive production values. Prefer environment‑specific or remote config for releases.
- Review diffs for secrets before pushing.

## Architecture Overview
- MVVM with repositories and Room under `data/`.
- Compose UI organized by feature packages.
- Keep state immutable; perform side‑effects with coroutines in `ViewModel`/repositories`.

