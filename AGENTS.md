# Repository Guidelines

## Project Structure & Module Organization
- Kotlin sources live under `app/src/main/java/` and are grouped by feature (for example `ui/admin/` or `data/repository/`).
- Resources, themes, and manifest entries stay in `app/src/main/res/`.
- JVM unit tests sit in `app/src/test/`; instrumentation and Compose UI suites belong in `app/src/androidTest/`.
- Supabase Edge Functions (push notifications) reside in `supabase/functions/send-push/`.
- Root Gradle files (`build.gradle.kts`, `settings.gradle.kts`) coordinate modules and dependencies.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds a debug APK at `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` deploys the debug build to a connected device or emulator.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed debug build.
- `./gradlew testDebugUnitTest` executes JVM unit tests.
- `./gradlew connectedAndroidTest` runs instrumentation and Compose UI tests.
- `./gradlew lint` applies static analysis and formatting checks.

## Coding Style & Naming Conventions
- Kotlin code uses four-space indentation, idiomatic null-safety, and Material 3 composables.
- Keep composables small, stateless, and previewable; hoist state into view models.
- Match MVVM naming (`*ViewModel`, `*Repository`, `*Dao`, `*Entity`, `*Screen`).
- Prefer constructor injection; avoid singletons outside of DI frameworks.

## Testing Guidelines
- Use JUnit with AndroidX Compose testing; mock or fake network layers.
- Mirror test class names to their targets (e.g., `HomeCustomerViewModelTest`).
- Keep tests deterministic; guard async work with coroutines and test dispatchers.
- Run `./gradlew testDebugUnitTest` before commits; add `connectedAndroidTest` when UI behavior shifts.

## Commit & Pull Request Guidelines
- Follow Conventional Commits (e.g., `feat:`, `fix:`, `refactor:`, `chore:`) with concise scopes.
- PRs should summarize impact, reference issues, list verification steps, and attach UI captures for screen changes.
- Confirm builds, unit tests, and lint pass locally; highlight affected paths like `app/src/main/...`.

## Security & Configuration Tips
- Store the Android SDK path in `local.properties`; never commit keystores or secrets.
- Keep `google-services.json` limited to debug credentials.
- Inject Supabase URLs and keys via `BuildConfig` or environment-aware configuration files.

## Architecture Overview
- MVVM flow: repositories feed view models, which expose immutable UI state to composables.
- Coroutines manage async work; Room entities and DAOs reside under `data/`.
- Repositories should remain interface-driven to simplify testing.

## Agent-Specific Instructions
- Limit edits to necessary files and respect existing changes.
- Default to ASCII unless a file already uses Unicode.
- Avoid irreversible operations or credential changes; document non-obvious logic with concise comments.
