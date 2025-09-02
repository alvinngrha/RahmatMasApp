# Repository Guidelines

## Project Structure & Module Organization
- Android app module: `app/`
  - Source: `app/src/main/java/`
  - Resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests: unit in `app/src/test/`, instrumentation/UI in `app/src/androidTest/`.
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`.
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app config in `app/build.gradle.kts`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Launch main activity after install: `adb shell am start -n com.example.rahmatmas/.MainActivity`.
- Run unit tests (JVM): `./gradlew testDebugUnitTest`.
- Run instrumentation/UI tests (device/emulator): `./gradlew connectedAndroidTest`.
- Static analysis: `./gradlew lint`.

## Coding Style & Naming Conventions
- Language: Kotlin + Jetpack Compose + Material3; 4‑space indent; Kotlin standard style.
- Architecture: immutable UI state; coroutines for async; each screen has a `ViewModel` owning state.
- Naming: screens `...Screen`; state `...ViewModel`, `...ViewModelFactory`; data `...Repository`; Room `...Dao` / `...Entity`.
- Packages mirror features: e.g., `ui/admin/...`, `ui/customer/...`, `data/...`.

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation).
- Test naming mirrors the class under test, e.g., `HomeCustomerViewModelTest`.
- Run: unit via `./gradlew testDebugUnitTest`; instrumentation via `./gradlew connectedAndroidTest` (device/emulator required).

## Commit & Pull Request Guidelines
- Commits use Conventional Commits: `feat:`, `fix:`, `refactor:`, etc.
- PRs include: clear summary, linked issue (if any), test steps, and screenshots/GIFs for UI changes.
- Keep changes scoped; ensure CI/build is green before requesting review.

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores.
- Store Supabase URL/keys in `BuildConfig` for now; avoid committing sensitive production values. Prefer environment‑specific or remote config for releases.
- Review diffs for secrets before pushing.

## Architecture Overview
- MVVM with repositories; Compose UI organized by feature packages.
- Room entities/DAOs live under `data/`.
- Keep state immutable; perform side‑effects with coroutines in `ViewModel`/repositories.

