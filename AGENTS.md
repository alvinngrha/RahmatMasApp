# Repository Guidelines

## Project Structure & Module Organization
- Kotlin features live under `app/src/main/java/`, grouped by responsibility (for example `ui/admin/`, `data/repository/`).
- Shared themes, drawables, and the Android manifest are under `app/src/main/res/`.
- JVM unit tests live in `app/src/test/`; instrumentation and Compose UI tests sit in `app/src/androidTest/`.
- Supabase Edge Functions, including push notifications, reside in `supabase/functions/send-push/`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK into `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` pushes the latest debug build to a connected emulator or device.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed app for manual verification.
- `./gradlew testDebugUnitTest` executes JVM unit tests; add `--tests` filters for targeted suites.
- `./gradlew connectedAndroidTest` runs instrumentation and Compose UI suites on an attached device.
- `./gradlew lint` applies static analysis and formatting checks.

## Coding Style & Naming Conventions
- Use four-space indentation, idiomatic Kotlin null-safety, and Material 3 composables.
- Keep composables stateless and previewable; hoist state into view models.
- Follow MVVM naming: `*ViewModel`, `*Repository`, `*Dao`, `*Entity`, and `*Screen`.
- Prefer constructor injection via the existing DI framework; avoid ad-hoc singletons.

## Testing Guidelines
- Use JUnit and AndroidX Compose testing; mock or fake network layers for determinism.
- Mirror test class names to their subjects (for example `HomeCustomerViewModelTest`).
- Guard coroutines with test dispatchers and timeouts; keep tests deterministic and isolated.
- Run `./gradlew testDebugUnitTest` before commits and expand to `connectedAndroidTest` when UI flows change.

## Commit & Pull Request Guidelines
- Write Conventional Commits (for example `feat:`, `fix:`, `refactor:`, `chore:`) with concise scopes.
- Summaries should note impact, reference issues, and list verification steps; attach UI captures for visual changes.
- Confirm builds, unit tests, and lint pass locally; highlight affected paths such as `app/src/main/...`.

## Security & Configuration Tips
- Keep the Android SDK path only in `local.properties`; never commit keystores or secrets.
- Limit `google-services.json` to debug credentials and inject Supabase keys via `BuildConfig` or environment configs.
- Review Supabase policies regularly and keep push credentials rotated.

## Architecture Overview
- MVVM flow: repositories feed view models, which expose immutable UI state to composables.
- Coroutines orchestrate async work; Room entities and DAOs live under `app/src/main/java/data/`.
- Interfaces and dependency injection keep repositories testable, swappable, and suitable for end-to-end coverage.
