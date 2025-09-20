# Repository Guidelines

Follow these notes when contributing; they reflect current repo standards.

## Project Structure & Module Organization
- `app/src/main/java/`: Kotlin sources by feature (e.g., `ui/admin/...`, `ui/customer/...`).
- `app/src/main/res/`: Compose resources, theming, and manifests.
- `app/src/test/` and `app/src/androidTest/`: JVM unit suites and instrumentation/Compose UI suites.
- `supabase/functions/send-push/`: Supabase Edge Function for push notifications.
- Root `build.gradle.kts` and `settings.gradle.kts`: orchestrate modules and dependencies.

## Build, Test, and Development Commands
- `./gradlew assembleDebug`: create the debug APK in `app/build/outputs/apk/debug/`.
- `./gradlew installDebug`: push the APK to a connected device/emulator.
- `adb shell am start -n com.example.rahmatmas/.MainActivity`: launch the installed build.
- `./gradlew testDebugUnitTest`: run JVM unit tests.
- `./gradlew connectedAndroidTest`: execute instrumentation and Compose UI tests.
- `./gradlew lint`: apply static analysis and formatting checks.

## Coding Style & Naming Conventions
- Use Kotlin style with four-space indentation, idiomatic null-safety, and Material 3 composables.
- Keep composables small, stateless, and previewable; lift state into view models.
- Apply MVVM naming: `*ViewModel`, `*Repository`, `*Dao`, `*Entity`, `*Screen`.
- Prefer constructor injection and avoid tightly coupled singletons.

## Testing Guidelines
- Rely on JUnit and AndroidX Compose testing; mock or fake network layers.
- Mirror test class names to targets (e.g., `HomeCustomerViewModelTest`).
- Keep tests deterministic; guard asynchronous work with coroutines and dispatchers.
- Run `./gradlew testDebugUnitTest` before PRs; add `connectedAndroidTest` when UI behavior changes.

## Commit & Pull Request Guidelines
- Write Conventional Commit messages (`feat:`, `fix:`, `refactor:`, `chore:`) with concise scopes.
- In PRs, summarize impact, link issues, list verification steps, and attach UI captures when screens change.
- Confirm builds, tests, and lint pass locally; note affected paths like `app/src/main/...`.

## Security & Configuration Tips
- Store the Android SDK path in `local.properties`; never commit keystores or secrets.
- Keep `google-services.json` restricted to debug credentials.
- Inject Supabase URLs and keys through `BuildConfig` or environment-aware config files.

## Architecture Overview
- MVVM guides flow: repositories → view models → composables.
- Coroutines manage async work; expose immutable UI state to the presentation layer.
- Room entities and DAOs live under `data/`; keep repositories interface-driven for testing.

## Agent-Specific Instructions
- Limit edits to necessary files and respect pre-existing changes.
- Default to ASCII unless a file already uses Unicode.
- Avoid irreversible operations or credential changes; document non-obvious logic with concise comments.
