# Repository Guidelines

## Project Structure & Module Organization
- Kotlin sources under `app/src/main/java/` are feature-scoped (for example, `ui/customer/`, `ui/admin/`, `data/repository/`).
- Shared Android resources, themes, and manifests reside in `app/src/main/res/`.
- JVM unit tests live in `app/src/test/`, while instrumentation and Compose UI suites belong in `app/src/androidTest/`.
- Supabase Edge Functions, such as push delivery logic, are versioned in `supabase/functions/send-push/`; review RLS implications when editing.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the distributable APK at `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` deploys the latest build to a connected Android device.
- Launch a debug build with `adb shell am start -n com.example.rahmatmas/.MainActivity` for smoke verification.
- `./gradlew testDebugUnitTest` runs JVM tests; scope runs via `--tests ClassNameTest`.
- Execute on-device suites using `./gradlew connectedAndroidTest`.
- Apply linting and style rules with `./gradlew lint`.

## Coding Style & Naming Conventions
- Use four-space indentation and idiomatic Kotlin null-safety, coroutines, and Material 3 composables.
- Hoist mutable UI state into `*ViewModel` classes; keep repositories, DAOs, and utilities within their feature package.
- Follow MVVM naming patterns such as `CustomerRepository`, `OrderDao`, and `DashboardScreen`.
- Favor brief comments only for non-obvious logic; keep resources and strings localized near their usage.

## Testing Guidelines
- Write JVM tests with JUnit4 and coroutine test dispatchers, mirroring class names (for example, `HomeCustomerViewModelTest`).
- Mock Supabase or other network collaborators to maintain deterministic runs.
- Run `./gradlew testDebugUnitTest` before pushing; plan `./gradlew connectedAndroidTest` whenever behaviour changes touch UI flows.

## Commit & Pull Request Guidelines
- Use Conventional Commits (for example, `feat(customer): add loyalty badge`) and link relevant issues.
- Confirm `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, and `./gradlew lint` succeed before opening a PR.
- Summarize affected paths (for example, `app/src/main/...`) and call out Supabase function updates explicitly.
- Attach emulator screenshots for visible UI changes and list any follow-up actions or dependencies in the PR description.

## Security & Configuration Tips
- Keep `local.properties`, keystores, and secrets out of version control.
- Inject Supabase credentials via `BuildConfig` or environment configuration rather than hardcoding values.
- When editing `supabase/functions/send-push/`, review Row Level Security policies and rotate push credentials as required.
