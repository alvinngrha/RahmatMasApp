# Repository Guidelines

## Project Structure & Module Organization
- Kotlin source lives under `app/src/main/java/`, organized by feature (for example `ui/admin/`, `data/repository/`).
- Shared resources such as themes, drawables, and manifest files sit in `app/src/main/res/`.
- JVM unit tests belong in `app/src/test/`; instrumentation and Compose UI tests go in `app/src/androidTest/`.
- Supabase Edge Functions, including push notification logic, reside in `supabase/functions/send-push/`.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK into `app/build/outputs/apk/debug/` for distribution.
- `./gradlew installDebug` installs the latest debug build onto a connected emulator or device.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed app for manual smoke checks.
- `./gradlew testDebugUnitTest` executes JVM unit suites; apply `--tests` to target specific classes.
- `./gradlew connectedAndroidTest` runs instrumentation and Compose UI flows on attached hardware.
- `./gradlew lint` enforces static analysis and formatting expectations.

## Coding Style & Naming Conventions
- Prefer four-space indentation, idiomatic Kotlin null-safety, and Material 3 composables.
- Keep composables stateless and previewable; hoist state into view models named `*ViewModel`.
- Follow MVVM naming: `*Repository`, `*Dao`, `*Entity`, `*Screen`; rely on constructor injection via the existing DI setup.

## Testing Guidelines
- Use JUnit and AndroidX Compose testing; mock or fake network boundaries for deterministic outcomes.
- Mirror test class names to their subjects (for example `HomeCustomerViewModelTest`).
- Guard coroutine tests with test dispatchers and timeouts; maintain isolation and idempotence.
- Run `./gradlew testDebugUnitTest` before committing; expand to `connectedAndroidTest` when UI flows change.

## Commit & Pull Request Guidelines
- Write Conventional Commits (for example `feat:`, `fix:`, `chore:`) with concise scopes and relevant issue references.
- Confirm builds, unit tests, and lint pass locally; include verification steps and attach UI captures for visual updates.
- Describe affected paths (e.g., `app/src/main/...`) and mention Supabase changes when applicable.

## Security & Configuration Tips
- Keep the Android SDK path only in `local.properties`; never commit keystores or secrets.
- Restrict `google-services.json` to debug credentials; inject Supabase keys through `BuildConfig` or environment configs.
- Review Supabase policies regularly and rotate push credentials alongside Edge Function updates.

## Architecture Overview
- MVVM structure: repositories feed view models, which expose immutable UI state to composables.
- Coroutines coordinate async work; Room entities and DAOs live under `app/src/main/java/data/`.
- Interfaces and dependency injection keep repositories testable, swappable, and friendly to end-to-end coverage.
