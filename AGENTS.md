# Repository Guidelines

## Project Structure & Module Organization
Android sources live in `app/src/main/java/`, organized by feature folders such as `ui/customer/` or `data/repository/`. Shared XML resources, themes, and manifests reside in `app/src/main/res/`. JVM unit tests belong in `app/src/test/`, while instrumentation and Compose UI suites use `app/src/androidTest/`. Supabase Edge Functions sit under `supabase/functions/` (for example `supabase/functions/send-push/`). Keep repositories, DAOs, and view models inside their feature package to maintain MVVM separation.

## Build, Test, and Development Commands
- `./gradlew assembleDebug` builds the debug APK at `app/build/outputs/apk/debug/`.
- `./gradlew installDebug` installs the debug variant onto a connected device or emulator.
- `adb shell am start -n com.example.rahmatmas/.MainActivity` launches the installed build for smoke checks.
- `./gradlew testDebugUnitTest` runs JVM unit tests (optionally narrow scope with `--tests ClassNameTest`).
- `./gradlew connectedAndroidTest` executes device/emulator suites.
- `./gradlew lint` enforces Android and Kotlin lint rules.

## Coding Style & Naming Conventions
Use four-space indentation and idiomatic Kotlin null-safety. Favor Material 3 composables and hoist mutable state into `*ViewModel` classes. Name layers with clear MVVM terms (`CustomerRepository`, `OrderDao`, `DashboardScreen`). Localize resources near their usage and keep shared components in dedicated feature folders. Add comments only when logic is non-obvious.

## Testing Guidelines
Write JUnit4 tests with coroutine test dispatchers, mirroring source class names (e.g., `HomeCustomerViewModelTest`). Mock Supabase or other network layers to keep tests deterministic. Always run `./gradlew testDebugUnitTest` before pushing, and schedule `./gradlew connectedAndroidTest` when UI behavior changes.

## Commit & Pull Request Guidelines
Adopt Conventional Commits such as `feat(customer): add loyalty badge` and link related issues. Before opening a PR, verify `./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, and `./gradlew lint` all pass. Summarize impacted paths (e.g., `app/src/main/...`), call out Supabase updates explicitly, attach emulator screenshots for UI changes, and list follow-up actions or dependencies.

## Security & Configuration Tips
Never commit `local.properties`, keystores, or secrets. Inject Supabase credentials via `BuildConfig` or environment-driven config, not hard-coded literals. When editing `supabase/functions/send-push/`, review Row Level Security policies and rotate push credentials as needed.
