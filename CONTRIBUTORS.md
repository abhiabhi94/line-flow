# Contributing to LineFlow

Thanks for your interest in contributing! This guide will help you get set up.

## Prerequisites

- **JDK 17** or higher — ensure `JAVA_HOME` is set in your shell profile (e.g. `~/.zshrc`, `~/.bashrc`)
- **[pre-commit](https://pre-commit.com/)** — install via `pip install pre-commit` (preferably in a virtual environment)

## Getting Started

1. **Fork and clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/line-flow.git
   cd line-flow
   ```

2. **Install pre-commit hooks**
   ```bash
   pre-commit install
   ```

3. **Build the project**
   ```bash
   ./gradlew assembleDebug                    # Android debug APK
   ./gradlew :web:wasmJsBrowserDistribution   # browser build -> web/build/dist/wasmJs/productionExecutable
   ```

4. **Run it**
   ```bash
   ./gradlew installDebug                     # on a connected device or emulator
   ./gradlew :web:wasmJsBrowserDevelopmentRun # in your browser, with hot reload
   ```

## Project layout

The game is one Kotlin Multiplatform code base built with Compose
Multiplatform: `shared/` holds the whole game (screens, levels, rules) with
small `androidMain`/`wasmJsMain` source sets for storage, music and haptics;
`app/` is the Android application shell; `web/` is the browser entry point.
See `AGENTS.md` for the map.

## Running Tests

```bash
# Run all unit tests (level validation, on the host JVM)
./gradlew :shared:jvmTest

# Run a specific test class
./gradlew :shared:jvmTest --tests "app.curious.lineflow.LevelValidationTest"

# Browser smoke test + screenshots (needs Node 22 and Playwright: npm i -g playwright && npx playwright install chromium)
./gradlew :web:wasmJsBrowserDistribution
node tool/screenshot.mjs --levels 1,20 --settings --hint --stroke 0,1,2,0   # -> shots/
```

The instrumented smoke test in `app/src/androidTest` runs on an emulator
(`./gradlew connectedDebugAndroidTest`, and the "Emulator smoke test"
workflow on every pull request).

## Designing Levels

`shared/src/commonMain/kotlin/app/curious/lineflow/Graph.kt` is generated; do not edit
it by hand. The levels live in `.scripts/leveldesign/catalog.py`, and the
toolkit around it (Python 3.9 or newer) checks every level the way the game
plays it:

```bash
# Verify all 60 levels and print the difficulty table
python3 .scripts/leveldesign/build.py

# Also render preview sheets (needs Pillow: pip install pillow)
python3 .scripts/leveldesign/build.py --png /tmp/levels

# Regenerate Graph.kt (refuses if any level has a problem)
python3 .scripts/leveldesign/build.py --write
```

Each level must be connected with 0 or 2 odd dots, have a solution found by
Hierholzer's algorithm and replayed line by line, and fit the smallest phone
we support. That phone is 360x640dp with a status bar and 3-button
navigation; after the top bar, status strip and bottom margin the canvas is
360x400dp, and dot centres keep a 40dp content margin inside it, so they
span 280x320dp. The game never reserves centring space below the drawing
at the expense of that canvas (`PlayfieldSpec.minPlayHeight`). All spacing
numbers are measured on it: dots at
least 56dp apart, no dot within 36dp of a line it is not on, and lines at
least 30 degrees apart at every dot. The game shrinks its touch radius on
cramped screens so hit circles never overlap; those numbers guarantee it
never drops below 25dp, and it stays at the full 32dp on phones about 410dp
wide or more. `geometry.py` and `LevelValidationTest` share these constants. Levels are ordered
by a difficulty score (lines drawn plus the share of random strokes that get
stuck), and the build fails if a level is easier than the one before it.
A level's id is its position in that order, and saved progress is keyed by
id, so a change that reorders the catalog moves players' completed and
unlocked levels with the positions, not the figures. Treat the printed
difficulty table as part of the review for any catalog change.
Hint text is derived from the geometry, so it always names the right dot and
direction. The same rules are enforced again by `LevelValidationTest`.

## Code Style

- Follow the existing patterns in the codebase
- Keep changes focused and minimal

Pre-existing findings are recorded in `config/ktlint/baseline-*.xml` and
`config/detekt/baseline-*.xml`; new code must be clean. ktlint follows the
`android_studio` code style (`.editorconfig`).

The project uses three linting tools that run automatically via pre-commit hooks:

| Tool | Command | What it checks |
|------|---------|---------------|
| **Android Lint** | `./gradlew lint` | Android-specific issues, performance, security |
| **ktlint** | `./gradlew ktlintCheck` | Kotlin code style and formatting |
| **detekt** | `./gradlew detekt` | Static analysis, code smells, complexity |

To auto-format code with ktlint:
```bash
./gradlew ktlintFormat
```

To run all checks manually:
```bash
./gradlew lint ktlintCheck detekt
```

## Submitting Changes

1. Create a new branch for your feature or fix
2. Make your changes
3. Ensure tests pass
4. Open a pull request with a clear description of what you changed and why

We appreciate all contributions, big or small!
