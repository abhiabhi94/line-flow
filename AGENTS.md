# AGENTS.md

Guidelines for every coding agent that works on this repository. This is the
single source of truth: PR-Agent (the PR review workflow) and Codex CLI read
`AGENTS.md` natively, Claude Code reads it through the `@AGENTS.md` import in
`CLAUDE.md`, and any other tool that supports `AGENTS.md` picks it up as is.
Edit this file only; do not duplicate its content into per-agent files.

## Project overview

**LineFlow** is a relaxing one-line puzzle game: draw every line of a figure
in a single stroke without lifting your finger or retracing (Eulerian paths).
60 hand-designed, machine-verified levels, hints, a tutorial, background
music and haptics. Kotlin + Compose Multiplatform: the same UI ships as an
Android app (Play Store) and as a browser app (GitHub Pages, Kotlin/Wasm).

## Modules

```
shared/   Kotlin Multiplatform library: the whole game (UI, levels, rules).
          commonMain  screens, Graph.kt (generated levels), theme, App()
          androidMain SharedPreferences store, MediaPlayer music, Vibrator haptics, res/raw music
          wasmJsMain  localStorage store, HTML Audio music, navigator.vibrate haptics
          jvmMain     no-op music/haptics so the tests run on the host JVM
          jvmTest     LevelValidationTest (+ PlayfieldLayoutTest): geometry & solvability of every level
app/      Android application: MainActivity, launcher icons, manifest, instrumented smoke test.
web/      Browser entry point (ComposeViewport), index.html, Wasm distribution.
.scripts/leveldesign/   Python level catalog + verifier + Graph.kt generator.
tool/screenshot.mjs     Headless-Chromium driver for the web build (smoke test + screenshots).
```

Platform differences go through `expect`/`actual` (`BackgroundMusicManager`,
`rememberHaptics`) or an interface the entry point implements
(`KeyValueStore` for progress). `AppConfig.unlockAllLevels` is set by the
Android debug build only.

## Build & development commands

```bash
./gradlew :shared:jvmTest                  # level validation tests (host JVM, no emulator)
./gradlew assembleDebug                    # Android debug APK (app.curious.lineflow.debug, all levels unlocked)
./gradlew installDebug                     # onto a connected device / emulator
./gradlew :web:wasmJsBrowserDistribution   # browser build -> web/build/dist/wasmJs/productionExecutable
./gradlew :web:wasmJsBrowserDevelopmentRun # dev server with hot reload (local machine)
./gradlew lint ktlintCheck detekt          # all linters (also run by pre-commit)
./gradlew ktlintFormat                     # auto-format

node tool/screenshot.mjs --levels 1,20 --settings              # phone screenshots -> shots/
node tool/screenshot.mjs --levels 1 --hint --stroke 0,1,2,0    # hint + trace level 1 -> win overlay
node tool/screenshot.mjs --levels 60 --viewport 1440x900       # desktop layout

python3 .scripts/leveldesign/build.py            # verify all levels, print the difficulty table
python3 .scripts/leveldesign/build.py --write    # regenerate shared/.../Graph.kt
```

Toolchain pins live in `gradle/libs.versions.toml` (Kotlin, Compose
Multiplatform, AGP) and `gradle/wrapper/gradle-wrapper.properties`; bump them
together. `compileSdk` is set in both `app/` and `shared/` and mirrored by
`.claude/hooks/session-start.sh`.

## Cloud sessions & visual verification

Claude Code on the web has no Android emulator (no KVM). The stand-in is the
**web build + headless Chromium**: `tool/screenshot.mjs` serves the Wasm
distribution, drives the app (tutorial, level list, any level, hint, a traced
solution, settings) at 390×844 and writes PNGs to `shots/`; `--viewport WxH`
renders a desktop window. It exits 1 on any uncaught exception, so it doubles
as the CI smoke test (`.github/actions/web-smoke`, job "Web smoke &
screenshots"). See `docs/cloud-dev.md` for how the container is set up and a
porting checklist for other repos.

The web target is also a public build: `.github/workflows/pages.yml` deploys
it to GitHub Pages (https://abhiabhi94.github.io/line-flow/) on every push to
`main`. The shipped store platform is still Android.

Compose paints to a canvas, so the driver cannot query a DOM. Tappable
widgets carry `Modifier.probe("<tag>")` (`UiProbe.kt`), which records their
window bounds; the web entry point exposes them as
`window.lineflowProbe.bounds(tag)`. **Give any new button a probe tag** and
extend the script's walk if a new screen appears.

## Levels

`shared/src/commonMain/kotlin/app/curious/lineflow/Graph.kt` is generated; do
not edit it by hand. Levels live in `.scripts/leveldesign/catalog.py`; the
toolkit verifies connectivity, 0 or 2 odd dots, a replayed one-stroke
solution, spacing on the smallest supported phone (360×640dp) and a monotonic
difficulty order, then writes one `levelN()` function per level (a single
initializer would exceed the JVM's 64 KB method limit). Level ids are
positions in that order and saved progress is keyed by id. The same rules are
enforced by `LevelValidationTest`. See `CONTRIBUTORS.md` for the numbers.

## Build types

| Build   | Application id                 | Levels                        |
|---------|--------------------------------|-------------------------------|
| debug   | `app.curious.lineflow.debug`   | all unlocked (`AppConfig`)    |
| release | `app.curious.lineflow`         | locked until previous cleared |
| web     | –                              | locked; the harness seeds `localStorage` to unlock |

Release signing reads `keystore.properties` (gitignored); without it the
release build is unsigned. Never commit keystores or `keystore.properties`.

## Testing

- `./gradlew :shared:jvmTest` runs the level and layout tests on the host.
- `app/src/androidTest` holds the UI Automator smoke test that the
  `emulator-smoke.yml` workflow runs on a KVM emulator (PRs and manual runs).
- `tool/screenshot.mjs` is the browser smoke test; CI runs it on every push
  and PR and uploads `shots/`.
- New game logic ships with a test in `shared/src/jvmTest`.

## Continuous integration

- `ci.yml`: unit tests + debug APK (uploaded as `lineflow-debug-apk`, 14
  days) and the web smoke job (`screenshots` artifact).
- `lint.yml`: Android Lint, ktlint, detekt.
- `emulator-smoke.yml`: instrumented tests + emulator screenshots.
- `pages.yml`: GitHub Pages deploy of the browser build from `main`
  (repo setting Settings → Pages → Source = "GitHub Actions", once).
- `pr-review.yml`: thin caller of the account-wide PR-Agent review in
  `abhiabhi94/.github`; needs the `OPENROUTER_API_KEY` repository secret.
  Comment `/review`, `/improve`, `/describe` or `/ask <question>` on a PR to
  run it on demand. PR-Agent reads this file from the default branch.

## Conventions

- Kotlin, Compose Material 3, the dark palette in `ui/theme/Color.kt`; no
  hardcoded colours in screens.
- ktlint uses the `android_studio` code style (`.editorconfig`), Composables
  are PascalCase. detekt config is `config/detekt/detekt.yml`.
- Pre-existing findings are recorded in `config/ktlint/baseline-*.xml` and
  `config/detekt/baseline-*.xml`; **new code must be clean**. Regenerate a
  baseline (`ktlintGenerateBaseline`, `detektBaseline`) only when
  deliberately accepting old code, never to hide a new finding.
- Common code must stay platform-free: no `java.*`/`android.*` imports in
  `commonMain` (use `kotlin.math`, `expect`/`actual`).
- Keep changes focused; do not reformat files you are not otherwise touching.

## Code review

Applies to any agent reviewing a pull request (PR-Agent via
`pr-review.yml`, Claude Code via `/code-review`, or a human-driven session).
Review only the diff against the base branch; do not re-review untouched code.

- Judge the change against the sections above: the module layering
  (platform code only in `androidMain`/`wasmJsMain`, common code
  platform-free), the level pipeline (Graph.kt never edited by hand; catalog
  changes come with the difficulty table), and the testing rules.
- Flag anything that would fail CI: a lint/ktlint/detekt finding outside the
  baselines, a broken web build, a probe tag missing on a new tappable
  widget (the smoke test cannot reach it), a change that would lose players'
  saved progress (SharedPreferences file `lineflow_progress`, keys in
  `GameProgressRepository`).
- Prefer concrete, actionable comments with the file and line; skip style
  nits the linters already enforce.
