# Repository Guidelines

## Project Structure & Module Organization
- Kotlin sources live in `app/src/main/java/` and follow a feature-first layout (for example `ui/admin/`, `data/repository/`).
- Shared resources, themes, and the manifest stay under `app/src/main/res/`.
- JVM unit tests reside in `app/src/test/`; instrumentation and Compose UI tests go in `app/src/androidTest/`.
- Supabase Edge Functions such as push notifications live at `supabase/functions/send-push/`.
- Root Gradle scripts (`build.gradle.kts`, `settings.gradle.kts`) coordinate modules and dependency catalogs.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK at `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` deploys the debug build to a connected emulator or device.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed app.
- `./gradlew testDebugUnitTest` runs JVM unit tests; add `--tests` filters for targeted runs.
- `./gradlew connectedAndroidTest` executes instrumentation and Compose UI suites.
- `./gradlew lint` applies static analysis and formatting checks.

## Coding Style & Naming Conventions
- Use four-space indentation, idiomatic Kotlin null-safety, and Material 3 composables.
- Keep composables stateless and previewable; lift state into view models.
- Follow MVVM naming (`*ViewModel`, `*Repository`, `*Dao`, `*Entity`, `*Screen`).
- Prefer constructor injection via the existing DI framework; avoid ad-hoc singletons.

## Testing Guidelines
- Rely on JUnit with AndroidX Compose testing; mock or fake network layers for determinism.
- Mirror test class names to their subjects (e.g., `HomeCustomerViewModelTest`).
- Guard coroutines with test dispatchers and timeouts; keep tests deterministic and isolated.
- Run `./gradlew testDebugUnitTest` before commits; expand to `connectedAndroidTest` when UI flows change.

## Commit & Pull Request Guidelines
- Write Conventional Commits (`feat:`, `fix:`, `refactor:`, `chore:`) with concise scopes.
- Summarize impact, reference issues, and list verification steps in PRs; attach UI captures for visual changes.
- Confirm builds, unit tests, and lint pass locally; highlight affected paths such as `app/src/main/...`.

## Security & Configuration Tips
- Keep the Android SDK path in `local.properties`; never commit keystores or secrets.
- Limit `google-services.json` to debug credentials and inject Supabase keys via `BuildConfig` or environment configs.

## Architecture Overview
- MVVM flow: repositories feed view models, which expose immutable UI state to composables.
- Coroutines orchestrate async work; Room entities and DAOs live under `app/src/main/java/data/`.
- Interfaces and dependency injection keep repositories testable and swappable.

## Agent-Specific Instructions
- Edit only necessary files, respect existing changes, and favor ASCII unless the file already uses Unicode.
- Avoid irreversible operations or credential changes; document non-obvious logic with concise comments when needed.
