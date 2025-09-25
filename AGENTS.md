# Repository Guidelines

## Project Structure & Module Organization
Kotlin sources reside under `app/src/main/java/`, grouped by feature (for example `ui/admin/` and `data/repository/`). Android resources, themes, and manifests live in `app/src/main/res/`. JVM tests belong in `app/src/test/`, while instrumentation and Compose UI suites stay in `app/src/androidTest/`. Supabase Edge Functions, including push logic, are under `supabase/functions/send-push/`. Keep feature modules cohesive and avoid cross-feature coupling.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a distributable debug APK in `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` flashes the latest debug build onto a connected device or emulator.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed debug app for smoke checks.
- `./gradlew testDebugUnitTest` runs JVM unit tests; append `--tests SomeClassTest` to scope execution.
- `./gradlew connectedAndroidTest` executes instrumentation and Compose UI flows.
- `./gradlew lint` enforces static analysis, style, and formatting.

## Coding Style & Naming Conventions
Use four-space indentation and idiomatic Kotlin null-safety. Prefer stateless Material 3 composables and hoist state into `*ViewModel` classes supplied via existing DI. Follow MVVM names such as `*Repository`, `*Dao`, `*Entity`, and `*Screen`. Keep shared utilities in feature-specific packages; document non-obvious logic with concise comments.

## Testing Guidelines
Adopt JUnit with coroutine test dispatchers and AndroidX Compose testing for UI flows. Mirror test class names to their targets (e.g., `HomeCustomerViewModelTest`). Mock or fake network interactions so suites remain deterministic. Run `./gradlew testDebugUnitTest` before pushing, and schedule `./gradlew connectedAndroidTest` when behavior or UI changes impact devices.

## Commit & Pull Request Guidelines
Write Conventional Commits (`feat:`, `fix:`, `chore:`) with focused scopes and issue references. Confirm builds, tests, and lint pass locally before opening pull requests. Summarize affected paths (for example `app/src/main/...`), note Supabase function updates, and attach emulator screenshots for visual changes.

## Security & Configuration Tips
Keep the Android SDK path in `local.properties`, exclude keystores, and rely on debug `google-services.json`. Inject Supabase keys through `BuildConfig` or environment configuration. Review Supabase Row Level Security policies regularly and rotate push credentials alongside Edge Function updates.

## Architecture Overview
The app follows MVVM: repositories feed view models, which expose immutable UI state to composables. Coroutines orchestrate async work, while Room entities and DAOs live under `app/src/main/java/data/`. Dependency injection keeps components modular and testable; favor interface-driven design for repositories and services.
