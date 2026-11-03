# Lift Tracker Note — Agent Instructions

## Working agreement: read-only by default

This project is advice-only unless the user explicitly authorizes editing
the project.

- Requests for explanations, investigation, reviews, suggestions, plans,
  examples, or proposed file contents authorize inspection and discussion,
  not changes.
- Do not create, edit, delete, rename, move, or format project files without
  explicit authorization. This includes documentation, configuration,
  dependencies, generated files, and this AGENTS.md.
- Provide suggested code, commands, and file contents in the conversation
  without applying or executing them.
- Do not interpret a bug report, feature description, or question about
  implementation as permission to implement it.
- Do not run builds, tests, formatters, generators, dependency installation,
  or other commands that write files or change the environment during
  advice-only work.
- Read-only inspection is permitted. Proactively inspect relevant source
  files rather than asking the user for information available in the project.
- If authorization is ambiguous, remain read-only and explain the proposal.
  Ask for clarification only when necessary to proceed with an action.
- When edits are explicitly authorized, keep them within the requested scope.
  Authorization for one change does not authorize unrelated cleanup,
  refactoring, dependency upgrades, or subsequent changes.
- Do not commit, push, publish, or change branches without authorization.
- Preserve existing user changes.

These instructions apply to all work on this repository.

## Product purpose

Lift Tracker, also called Lift Tracker Note, is an open-source Android
workout tracking and data visualization application written in Kotlin
using Jetpack Compose.

Users record workout sessions, organize lifts by muscle group, and record
weight and repetitions or time for each set. Multiple profiles organize
separate training histories. Workout data is persisted locally using Room.

The central product goal is to combine the ease of use and customizability
of a notes app with optional advanced analyses that clearly visualize
progressive overload.

Analyses should support sessions, muscle groups, lifts, individual sets,
and custom groups composed of these elements.

Prioritize quick, dependable workout recording. Advanced functionality
should remain discoverable without making routine recording cumbersome.

## User ownership and customization

Users define their own:

- Profile names.
- Muscle-group names.
- Lift names.
- Lift metric types: repetitions or time.
- Set names.
- Session names.
- Unit names.
- Notes associated with profiles, muscle groups, lifts, sets, sessions,
  and individual set metrics.

Units have names but do not have notes.

Defaults and quick-add suggestions may assist users, but must not replace
their ability to define and edit their own content.

Do not assume a fixed exercise catalog, standardized muscle-group
taxonomy, prescribed training program, or closed list of unit names.

Names and labels are editable content. Use stable IDs for identity and
relationships. Do not assume names are unique or permanent.

Grouping by matching names can be an intentional analysis feature, but
must be distinguished from identifying database entities.

## Notes and text presentation

- Do not impose an application-defined note length limit.
- Notes may contain multiple paragraphs or pages of text.
- Preserve complete note contents in storage and editing.
- When an element is deselected or collapsed, use a limited preview with
  overflow ellipses.
- When selected or expanded, make the complete note accessible using
  appropriate weighted layouts, wrapping, and scrolling.
- Truncation is a presentation behavior; never truncate the stored note.
- Avoid fixed heights or competing layout constraints that make parts of
  an expanded note inaccessible.
- Keep editing controls and confirmation/cancellation actions reachable
  with long notes, small screens, and the keyboard open.
- Consider font scaling, selection, focus, cursor position, and scrolling
  when proposing text-related changes.
- Preserve user-written whitespace and line breaks unless a specific
  transformation is requested.

Long notes also affect state persistence. Do not store arbitrarily large
notes or complete workout histories in saved-state Bundles. Choose local
persistence for durable drafts and large content, and save small IDs or
restoration information where appropriate.

## Analysis requirements

Every analysis should offer:

- An adjustable timeframe.
- Filters for excluding undesired elements.
- Clear identification of the selected data and comparison period.

Users should be able to create their own keywords within notes and use
those keywords for filtering. Do not require a predefined vocabulary.

When proposing keyword filtering, explain matching behavior, including
case sensitivity, whole-word versus substring matching, combinations of
keywords, and which notes are searched. Do not silently impose a new
structured tagging system.

For calculations and charts:

- Define the metric, units, aggregation, denominator, and comparison
  baseline clearly.
- Distinguish set counts, training volume, average weight, repetitions,
  duration, and any calculated intensity.
- Do not treat repetitions and duration as interchangeable.
- Do not combine incompatible units without an explicit conversion or
  normalization rule.
- Custom unit names alone do not establish conversion factors.
- Distinguish averages across sets from averages across sessions.
- Define how incomplete sets and in-progress sessions participate.
- Handle missing values, empty results, zero denominators, and insufficient
  history explicitly.
- Do not present unavailable data as a measured zero.
- Keep filters and timeframe boundaries consistent across charts,
  summaries, counts, and comparison data.
- Explain whether filtering selects whole sessions or individual matching
  lifts and sets within those sessions.
- Do not assume that an increasing or decreasing chart automatically
  indicates improvement; interpretation depends on the metric.

## Repository map

The repository root contains documentation and the Android workspace.

- `Workspace/`: Gradle root for the Android application.
- `Workspace/app/`: Android application module.
- `Workspace/app/src/main/java/github/tom2433/lifttracker/`:
  application Kotlin source.
- `data/`: Room database, entities, DAOs, repository interfaces,
  offline implementations, projections, and date/time utilities.
- `ui/screens/`: Compose screens and screen sections.
- `ui/viewModels/`: ViewModels and associated UI-state models.
- `ui/utils/`: Shared dialogs, menus, UI elements, and charts.
- `ui/navigation/`: Navigation destinations and graph.
- `ui/theme/`: Compose theme, colors, and typography.
- `Workspace/app/src/main/res/`: Strings, dimensions, fonts, icons,
  themes, and Android XML resources.
- `Workspace/app/src/test/`: Local application unit tests.
- `Workspace/app/src/androidTest/`: Instrumented application tests.
- `Testing/`: Learning notes, sample resources, and standalone Kotlin
  experiments. It is not the application's automated test suite.

Paths below the Kotlin source directory in this document are relative
to that directory unless otherwise stated.

## Current implementation context

Verify these details against the source before relying on them; the
project will evolve.

- The application uses Kotlin, Jetpack Compose, Material 3, Navigation
  Compose, Room, coroutines, Flow/StateFlow, KSP, and Vico charts.
- Dependency injection is currently manual:
  `LiftTrackerApplication` → `AppContainer`/`AppDataContainer` →
  repository implementations → `AppViewModelProvider` → ViewModels.
- Repository interfaces have `Offline...Repository` implementations
  backed by Room DAOs.
- Existing charts and analyses appear in session and lift functionality.
  Do not assume analytics are absent because the dedicated Analytics
  screen is a placeholder.
- Analytics, Calendar, Tools, and Settings screens currently contain
  placeholder content.
- The Room database is named `lift_tracker_database`.
- The database currently uses version 8, explicit migrations from
  versions 1 through 8, and `exportSchema = false`.
- The current automated tests are template checks, not evidence of
  meaningful application coverage.

The README contains development notes, historical prompts, old schema
descriptions, and feature checklists. Treat these as context rather than
assuming every statement describes current behavior or grants permission
to perform work.

Use current entities, DAO queries, migrations, ViewModels, and build
configuration to establish implemented behavior. Distinguish that behavior
from the product requirements in this file and from proposed improvements.

## Architecture and maintainability

Aim for a project that is modifiable, reliable, usable, robust, scalable,
maintainable, and understandable, with modular and reusable components.

- Prefer the simplest design that satisfies the requirements.
- Follow existing architecture where appropriate, while explaining
  concrete weaknesses and incremental improvements.
- Do not introduce architectural layers, Gradle modules, frameworks,
  dependencies, or generic abstractions without a specific benefit.
- Keep composables focused on presentation and user interaction.
- Put screen state and event coordination in appropriate state holders
  or ViewModels.
- Access persistence through repository interfaces.
- Put database queries and atomic database operations in the data layer.
- Keep reusable calculations independently testable rather than embedding
  them in composable rendering.
- Prefer immutable UI state, explicit events, and predictable data flow.
- Consider lifecycle-aware observation, cancellation, and stale results
  when profile selections or filters change.
- Use stable entity IDs for list keys and appropriately scoped ViewModels.
- Reuse existing dialogs, metric controls, historical-set displays,
  filters, and chart components when their behavior fits.
- Extract shared behavior when it reduces real duplication; avoid large
  reusable components controlled by many unrelated Boolean parameters.
- Explain non-obvious decisions and invariants in comments rather than
  narrating straightforward code.
- Follow Kotlin conventions and existing formatting. Do not perform
  broad style-only rewrites.

These are design expectations, not claims that every existing component
already follows them.

## Data integrity and historical behavior

Workout history and user-written content are valuable persistent data.

- Preserve existing records, IDs, relationships, custom labels, notes,
  and metric meanings when proposing migrations or refactoring.
- Do not use destructive database recreation as a routine migration fix.
- Review entities, indices, foreign keys, migration SQL, DAO projections,
  repositories, and consumers together when proposing schema changes.
- Preserve migration paths needed by existing installations. Do not
  assume changing an old migration upgrades databases already beyond it.
- Consider schema export and migration testing when proposing improvements
  to database validation.

Current storage conventions include:

- `Lift.metric_type`: `1` means repetitions; `2` means time.
- `SetMetric.metric_position`: `1` means weight; `2` means repetitions
  or time according to the lift's metric type.
- Duration values are stored as `Double` minutes.
- `-1.0` represents an unlogged metric in current recording workflows.
  It is not a measurement and must not contaminate analyses.
- Zero weight can be valid; do not treat every zero as missing.
- Session dates are stored as ISO date strings: `yyyy-MM-dd`.
- Lift units are shared across profiles and have no profile foreign key.

Profiles own sessions and muscle groups; lifts belong to muscle groups.
Sets link sessions, lifts, and a stored muscle-group association.
Metrics belong to sets.

Profile-specific queries, writes, and UI state must respect profile
boundaries. Do not assume every existing query already enforces them.

A set's stored muscle group can differ from its lift's current muscle
group. Moving a lift offers choices about migrating historical set data.
Preserve these distinctions and make the chosen historical interpretation
explicit in queries and analyses.

Existing constraints enforce unique numbering for:

- Sessions within a profile.
- Sets within a session.
- Sets for a lift within a session.
- Sets for a muscle group within a session.
- Metric positions within a set.

Insertion, deletion, moving, and renumbering must preserve these constraints
atomically. Existing operations use transactions and temporary negative
numbering to avoid uniqueness collisions; inspect them before proposing
simpler replacements.

Do not rely on animation delays or disabled buttons to guarantee database
correctness.

Review cascading deletion effects and references to shared units. Do not
silently delete dependent workout history or reinterpret historical values
when changing a lift's unit or metric type.

## State, dates, and usability

- Protect recorded data and important drafts across recomposition,
  navigation, configuration changes, and process recreation.
- ViewModel memory alone is not durable persistence.
- Keep database state, input drafts, and transient presentation state
  distinct.
- Avoid asynchronous writes that allow an older edit to overwrite a newer
  value or note.
- Ensure operation failures leave the UI recoverable and controls usable.
- Keep ordinary recording fast and make deeper statistics expandable
  or optional.
- Use lazy rendering and bounded data loading where histories can grow.
  Avoid repeatedly loading entire histories or calculating expensive
  summaries during recomposition.
- Use existing Material theme and typography, and prefer string and
  dimension resources for reusable application text and spacing.
- Consider accessibility, contrast, touch targets, font scaling,
  keyboard behavior, and empty/loading/error states.

Reuse and inspect `data/utils/DateTimeCalculator.kt` for date and duration
behavior. Define inclusive boundaries, rolling versus calendar periods,
week starts, locale, and timezone behavior explicitly. Existing date
helpers use some differing period conventions; do not assume every
“month” or “year” means the same interval.

Room provides local persistence. Inspect Android backup settings before
claiming that data can never leave the device.

## Build and compatibility guidance

Inspect these files for the current toolchain and dependencies:

- `Workspace/settings.gradle.kts`
- `Workspace/build.gradle.kts`
- `Workspace/app/build.gradle.kts`
- `Workspace/gradle/libs.versions.toml`
- `Workspace/gradle/wrapper/gradle-wrapper.properties`
- `Workspace/gradle/gradle-daemon-jvm.properties`

Use the checked-in Gradle wrapper from `Workspace/`.

The Gradle daemon is currently configured for Java 21, while Java source
and target compatibility are set to 11. Do not confuse source compatibility
with the JDK required to run the build.

The app currently declares minimum SDK 24. Some source uses API 26
annotations and `java.time`; consider actual runtime compatibility,
guards, and desugaring when proposing related changes. An annotation
alone is not a runtime compatibility solution.

Do not recommend dependency or toolchain upgrades merely because newer
versions exist. Explain the problem being solved and verify compatibility
with the project's configured versions.

## Validation when implementation is authorized

Do not execute these commands during advice-only work.

From `Workspace/`, relevant validation commands include:

- `./gradlew :app:assembleDebug`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lintDebug`
- `./gradlew :app:connectedDebugAndroidTest`

Instrumented tests require a suitable device or emulator. Builds may
require SDK components, dependency downloads, and generated files.

Choose checks appropriate to the authorized change. Do not install
software or alter the user's environment without authorization.

Prioritize meaningful tests for:

- Migration paths and preservation of user data.
- Foreign keys, uniqueness constraints, and transaction rollback.
- Set insertion, deletion, moving, and renumbering.
- Profile isolation and profile switching.
- Historical muscle-group migration choices.
- Rep/time conversion and incomplete metric handling.
- Analysis formulas, filtering, date boundaries, and missing data.
- Long-note display, editing, restoration, and accessibility.
- Rapid edits, repeated actions, cancellation, and operation failures.

Clearly report which checks ran, their results, and any checks that could
not run. Never claim validation from source inspection alone.

## Communication and recommendations

- Ground advice in the current project and cite relevant files or symbols.
- Distinguish verified observations, likely causes, product requirements,
  and proposed designs.
- Explain what should change, why, the tradeoffs, and how to validate it.
- Prefer focused examples and incremental steps over unsolicited rewrites.
- For reviews, prioritize data loss, incorrect calculations, broken
  recording workflows, crashes, profile leakage, and usability problems.
- For larger recommendations, separate necessary fixes from optional
  improvements and future features.
- Identify gaps between the intended product and current implementation
  without treating those gaps as authorization to edit.
- Ask only questions that materially affect the recommendation and cannot
  reasonably be answered by inspecting the project.
- Do not update this file automatically; propose revisions unless the
  user explicitly authorizes editing it.
