# Repository Guidelines

## Project Structure & Module Organization
Feature-centric Kotlin sources live under `app/src/main/java/`, grouped by packages such as `ui/customer/`, `ui/admin/`, and `data/repository/`. Android resources, themes, and manifests belong in `app/src/main/res/`. JVM unit tests reside in `app/src/test/`, while instrumentation and Compose UI suites live in `app/src/androidTest/`. Supabase Edge Functions, including push delivery logic, are versioned alongside the app in `supabase/functions/send-push/`.

## Build, Test, and Development Commands
`./gradlew assembleDebug` builds the distributable APK at `app/build/outputs/apk/debug/`. Use `./gradlew installDebug` to deploy the latest build to a connected device, then launch it with `adb shell am start -n com.example.rahmatmas/.MainActivity` for smoke checks. Run JVM tests through `./gradlew testDebugUnitTest`, and scope failures via `--tests ClassNameTest`. Execute on-device verification with `./gradlew connectedAndroidTest`. Apply linting and style rules using `./gradlew lint`.

## Coding Style & Naming Conventions
Indent Kotlin with four spaces and favour idiomatic null-safety, coroutines, and stateless Material 3 composables. Hoist mutable UI state into `*ViewModel` classes supplied by existing DI bindings. Follow MVVM naming (`*Repository`, `*Dao`, `*Entity`, `*Screen`) and keep utilities within their feature package. Document only non-obvious logic with brief comments.

## Testing Guidelines
Write JVM tests with JUnit4 and coroutine test dispatchers, mirroring class names (for example, `HomeCustomerViewModelTest`). Mock Supabase or other network collaborators to keep runs deterministic. Place Compose UI and instrumentation tests in `app/src/androidTest/`. Run `./gradlew testDebugUnitTest` before pushing, and plan `./gradlew connectedAndroidTest` whenever UI or behaviour changes touch devices.

## Commit & Pull Request Guidelines
Adopt Conventional Commits such as `feat(customer): …` or `fix(data): …` and link relevant issues. Before opening a PR, confirm `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, and `./gradlew lint` succeed. Summarise affected paths (for example, `app/src/main/...`), call out Supabase function updates explicitly, and attach emulator screenshots for visible UI tweaks. Note any follow-up actions or dependencies.

## Security & Configuration Tips
Keep `local.properties`, keystores, and secrets out of version control. Inject Supabase credentials via `BuildConfig` or environment configuration rather than hardcoding. When modifying `supabase/functions/send-push/`, review Row Level Security policies and rotate push credentials as needed.
