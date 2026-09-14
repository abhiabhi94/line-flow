---
name: run
description: Build the app for the browser (Kotlin/Wasm) and drive it in headless Chromium at a phone viewport to capture screenshots of the level list, settings, any level, a hint and a traced solution, then review the PNGs. Use to visually verify a UI change, check a level's rendering, or run the smoke test. The cloud container has no Android emulator (no KVM); this harness is the stand-in.
---

# Run & screenshot the app (cloud stand-in for the emulator)

The session container has no KVM, so no Android emulator. Instead the app is
built for the **web** (the same Compose code as Android, from `:shared`) and
rendered in headless Chromium at 390×844 @2x.

## Commands

```bash
./gradlew :web:wasmJsBrowserDistribution                        # ~1-2 min, output in web/build/dist/wasmJs/productionExecutable
node tool/screenshot.mjs --levels 1,20 --settings               # -> shots/*.png
node tool/screenshot.mjs --levels 1 --hint --stroke 0,1,2,0     # hint + trace level 1's solution -> win overlay
node tool/screenshot.mjs --tutorial --levels 3                  # first-launch tutorial overlay
node tool/screenshot.mjs --levels 60 --viewport 1440x900        # desktop window (GitHub Pages)
node tool/screenshot.mjs --levels 1 --dump                      # print the tags the UI exposes + positions
```

`--levels` takes level ids (1–60); their predecessors are seeded as completed
so the tiles are unlocked (`--completed 1-59` to seed differently). The dot
ids for `--stroke` are in the "One solution" comment above each level in
`shared/src/commonMain/kotlin/app/curious/lineflow/Graph.kt`. Then `Read` the
PNGs in `shots/` to review them.

Rebuild whenever `shared/` or `web/` changes; the script serves whatever is
in the distribution directory. `ANDROID_HOME`/`NODE_PATH` are set by the
session-start hook; if Gradle cannot find the SDK run
`.claude/hooks/session-start.sh` with `CLAUDE_CODE_REMOTE=true`.

## What it verifies

- Exit code 1 if the app threw (an uncaught Kotlin exception, a JS error, an
  error captured in composition) while being driven — treat that as a failing
  test. CI runs the same script via `.github/actions/web-smoke` (see `ci.yml`,
  job "Web smoke & screenshots") and uploads `shots/` as an artifact.
- Rendering is faithful: Compose bundles its default text font; the emoji
  fallback font (🔒 💡 ⚙️) comes from fonts.gstatic.com, which the script
  mirrors through Node (cached in `web/build/font-cache/`) because headless
  Chromium cannot reach it through the cloud proxy.

## Extending the driver

Compose paints to a canvas, so there is no DOM to query. Tappable widgets
carry `Modifier.probe("<tag>")` (see `UiProbe.kt` in `:shared`), which
records their window bounds; the web entry point exposes the registry as
`window.lineflowProbe.bounds(tag)` and the script clicks those coordinates
with real pointer events. Give a new button a `probe("...")` tag and use
`--dump` to see it. The playfield also publishes `node-<id>` for every dot,
which is how `--stroke` traces a solution.

Preferences are seeded through `localStorage` (`lineflow_progress.<key>`)
before boot — that is how the tutorial is skipped and music muted. Add new
prefs there rather than clicking through the UI.

## Gotchas (all already handled in the script — keep them when porting)

- Headless Chromium's default locale here is `en-US@posix`; Compose rejects
  it at boot ("Incorrect locale information provided"). The script sets
  `locale: 'en-US'` on the browser context.
- The Kotlin/Wasm toolchain installs its own `karma` fork from GitHub, which
  the proxy blocks; `build.gradle.kts` pins the npm registry release instead.
- Off-screen level tiles are not composed (lazy grid), so they have no
  bounds; the script scrolls the grid until the tile appears.
