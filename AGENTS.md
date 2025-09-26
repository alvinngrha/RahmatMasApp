# Repository Guidelines

## Project Structure & Module Organization
Kotlin sources live in `app/src/main/java/`, organized by feature modules such as `ui/customer/`, `ui/admin/`, and `data/repository/` so ownership stays clear. Android resources, themes, and manifests sit under `app/src/main/res/`. JVM unit tests belong in `app/src/test/`, instrumentation and Compose UI suites in `app/src/androidTest/`. Supabase Edge Functions, including push delivery logic, are stored in `supabase/functions/send-push/`; keep them versioned with app changes.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK at `app/build/outputs/apk/debug/` for distribution.
- `./gradlew installDebug` installs the latest debug build onto a connected emulator or device.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed debug app for smoke checks.
- `./gradlew testDebugUnitTest` runs JVM unit tests; add `--tests SomeClassTest` to scope.
- `./gradlew connectedAndroidTest` executes instrumentation and Compose UI flows on devices.
- `./gradlew lint` applies static analysis, formatting, and style rules.

## Coding Style & Naming Conventions
Indent with four spaces and embrace idiomatic Kotlin null-safety and coroutines. Prefer stateless Material 3 composables; hoist mutable state into `*ViewModel` classes injected through existing DI bindings. Use MVVM naming patterns (`*Repository`, `*Dao`, `*Entity`, `*Screen`) and keep utilities inside the owning feature package. Document only non-obvious logic with succinct comments.

## Testing Guidelines
Use JUnit4 with coroutine test dispatchers for JVM tests and AndroidX Compose testing for UI flows. Mirror test class names to their targets (for example `HomeCustomerViewModelTest`). Mock Supabase and network calls so tests stay deterministic. Run `./gradlew testDebugUnitTest` before pushing; plan `./gradlew connectedAndroidTest` for behavior or UI changes touching devices.

## Commit & Pull Request Guidelines
Write Conventional Commits (`feat:`, `fix:`, `chore:`) with precise scopes and issue references. Before opening PRs, ensure builds, tests, and lint pass locally. Summarize affected paths (e.g., `app/src/main/...`), flag Supabase function updates, and attach emulator screenshots for visible UI tweaks.

## Security & Configuration Tips
Keep `local.properties` private, exclude keystores, and rely on the debug `google-services.json`. Inject Supabase credentials via `BuildConfig` or environment configuration. Review Row Level Security policies regularly and rotate push credentials whenever the Edge Function changes.

## Architecture Overview
The app follows MVVM: repositories expose data to view models, which provide immutable UI state to composables. Coroutines coordinate background work, while Room entities and DAOs reside in `app/src/main/java/data/`. Dependency injection keeps modules testable; prefer interface-driven repositories and services to simplify stubbing.
