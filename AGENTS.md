# Repository Guidelines

## Project Structure & Module Organization
- Android app: `app/`
  - Source: `app/src/main/java/`
  - Resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests: unit `app/src/test/`, instrumentation/UI `app/src/androidTest/`.
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`.
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app `app/build.gradle.kts`.

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`.
- Install on device/emulator: `./gradlew installDebug`.
- Launch main activity: `adb shell am start -n com.example.rahmatmas/.MainActivity`.
- Run unit tests: `./gradlew testDebugUnitTest`.
- Run instrumentation/UI tests: `./gradlew connectedAndroidTest` (device/emulator required).
- Static analysis: `./gradlew lint`.

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose + Material 3; 4‑space indent; Kotlin official style.
- MVVM: immutable screen UI state; one `ViewModel` per screen.
- Naming: screens `...Screen`; state `...ViewModel`/`...ViewModelFactory`; data `...Repository`; Room `...Dao`/`...Entity`.
- Packages mirror features (e.g., `ui/admin/...`, `ui/customer/...`, `data/...`).
- Prefer clear, descriptive names; avoid one‑letter variables.

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation/UI).
- Test classes mirror targets (e.g., `HomeCustomerViewModelTest`).
- Focus on `ViewModel` logic, repositories, and Room queries.
- Run: `./gradlew testDebugUnitTest` and `./gradlew connectedAndroidTest`.

## Commit & Pull Request Guidelines
- Conventional Commits (e.g., `feat:`, `fix:`, `refactor:`, `chore:`).
- PRs: clear summary, linked issue, test steps; add screenshots/GIFs for UI changes.
- Keep scope narrow; ensure build, tests, and lint are green before review.

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores.
- Store Supabase URL/keys in `BuildConfig`; avoid production secrets in VCS.
- Review diffs for secrets; prefer environment‑specific/remote config for releases.

## Architecture Overview
- MVVM with repositories and Room under `data/`.
- Compose UI organized by feature packages.
- Use coroutines for side‑effects; keep UI state immutable.

