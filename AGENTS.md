# Repository Guidelines

## Project Structure & Module Organization
- Android app: `app/`
  - Source: `app/src/main/java/`, resources: `app/src/main/res/`
  - Firebase config: `app/src/google-services.json`
- Tests: unit `app/src/test/`, instrumentation/UI `app/src/androidTest/`
- Supabase Edge Function (TypeScript): `supabase/functions/send-push/`
- Build config: root `build.gradle.kts`, `settings.gradle.kts`; app `app/build.gradle.kts`

## Build, Test, and Development Commands
- Build debug APK: `./gradlew assembleDebug` → `app/build/outputs/apk/debug/`
- Install on device/emulator: `./gradlew installDebug`
- Launch main activity: `adb shell am start -n com.example.rahmatmas/.MainActivity`
- Run unit tests: `./gradlew testDebugUnitTest`
- Run instrumentation/UI tests (device/emulator required): `./gradlew connectedAndroidTest`
- Static analysis (lint): `./gradlew lint`

## Coding Style & Naming Conventions
- Kotlin + Jetpack Compose (Material 3); 4-space indent; follow Kotlin official style
- Architecture: MVVM; one `ViewModel` per screen; immutable UI state; coroutines for side effects
- Package by feature: e.g., `ui/admin/...`, `ui/customer/...`, `data/...`
- Naming: screens `*Screen`; state `*ViewModel`/`*ViewModelFactory`; data `*Repository`; Room `*Dao`/`*Entity`

## Testing Guidelines
- Frameworks: JUnit (unit), AndroidX + Compose testing (instrumentation/UI)
- Name tests after targets (e.g., `HomeCustomerViewModelTest`)
- Keep tests deterministic; avoid network/device flakiness in unit tests
- Run unit: `./gradlew testDebugUnitTest`; instrumentation: `./gradlew connectedAndroidTest`

## Commit & Pull Request Guidelines
- Use Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`)
- PRs include summary, linked issue, test steps; add screenshots/GIFs for UI changes
- Keep scope narrow; ensure build, tests, and lint are green before review

## Security & Configuration Tips
- Keep Android SDK path in `local.properties`; never commit keystores
- Store Supabase URL/keys via `BuildConfig`; no production secrets in VCS
- Prefer environment-specific or remote config; review diffs for secrets

## Architecture Overview
- MVVM with repositories and Room under `data/`
- Compose UI organized by feature packages
- Use coroutines for side effects; keep UI state immutable

