# Developing in Claude Code on the web (cloud sessions)

How this repo is set up so a cloud session (no Android emulator, no KVM) can
build, test, and **visually verify** the app — and what to copy to another
Compose Multiplatform game repo to get the same workflow.

## What the cloud container can and cannot do

| Capability                            | Status | Notes |
|---------------------------------------|--------|-------|
| Android SDK (platform + build-tools)  | ✅     | Installed by the session-start hook into `/opt/android-sdk` (~300 MB, cached with the container) |
| `./gradlew :shared:jvmTest`, linters  | ✅     | Same gates as CI |
| Android debug APK                     | ✅     | `./gradlew assembleDebug` (~2 min cold); CI uploads it as an artifact too |
| Web build + headless Chromium         | ✅     | `tool/screenshot.mjs` — phone *and* desktop (`--viewport`) screenshots, hint, traced solution, smoke test |
| Android emulator                      | ❌     | No `/dev/kvm`; an emulator would not boot usably |
| Release signing                       | ❌     | Needs `keystore.properties`, which is not in the container |

So the loop is: change code → `./gradlew :web:wasmJsBrowserDistribution` →
`node tool/screenshot.mjs --levels …` → read the PNGs. CI runs the same
script and uploads the screenshots as an artifact.

## Files that make this work

| File | Role |
|------|------|
| `AGENTS.md` + `CLAUDE.md` | One set of repo guidelines for every agent. `AGENTS.md` is the content (PR-Agent and Codex read it natively, incl. a "Code review" section); `CLAUDE.md` is a shim that starts with `@AGENTS.md` so Claude Code imports the same file. |
| `.claude/hooks/session-start.sh` + `.claude/settings.json` | Cloud-only SessionStart hook: Android cmdline-tools + platform + build-tools, `local.properties`, one Gradle warm-up (also downloads Node/Yarn for Kotlin/Wasm), Playwright/Chromium check, exports `ANDROID_HOME`/`NODE_PATH`. |
| `.claude/skills/run/SKILL.md` | Tells Claude how to build/screenshot/review in a session. |
| `tool/screenshot.mjs` | The driver: static server + emoji-font mirror + Playwright walk + smoke gate. |
| `shared/.../UiProbe.kt` + `web/.../Main.kt` | The app's side of the driver: `Modifier.probe(tag)` records widget bounds, `window.lineflowProbe.bounds(tag)` exposes them (Compose paints to a canvas, so there is no DOM to query). |
| `.github/actions/web-smoke/action.yml` | Composite action: build web, run the driver, upload `shots/`. Used by the `smoke` job in `ci.yml`. |
| `.github/workflows/pages.yml` | Deploys the browser build to the root of the `gh-pages` branch on every push to `main` (GitHub Pages serves that branch). |
| `.github/workflows/pr-preview.yml` | Deploys each PR's browser build to `pr-preview/pr-<n>/` on `gh-pages` (JamesIves deploy action with `target-folder`), comments the link, and removes the folder when the PR closes; `ci.yml` adds a sticky comment with the debug APK artifact. |
| `.github/workflows/pr-review.yml` | Short caller of the account-wide reusable PR-Agent review in `abhiabhi94/.github`. Identical in every repo. |
| `build.gradle.kts` (root) | Pins the Kotlin/Wasm tooling's `karma` to the npm registry release: the default is a GitHub tarball, which the cloud egress proxy blocks. |
| `.editorconfig` | ktlint code style (`android_studio`), Composable naming exemption, and `ktlint = disabled` for the generated `Graph.kt`. |

## Porting to another Compose Multiplatform game repo — checklist

1. Layout: a `shared` KMP library (`com.android.kotlin.multiplatform.library`
   + `wasmJs { browser() }` + `jvm()` for host tests), a thin `app`
   (`com.android.application`) and a `web` module with
   `wasmJs { binaries.executable() }`, `ComposeViewport` and `index.html`.
   AGP 9 does not allow `kotlin.multiplatform` and `com.android.application`
   in one module. Keep `dependencyResolutionManagement` in the default
   repositories mode (the Wasm toolchain adds Node/Yarn repos to the root
   project).
2. Copy `.claude/`, `tool/screenshot.mjs`, `.github/actions/web-smoke/`, the
   `smoke` job from `ci.yml`, `pages.yml`, `pr-review.yml`, the karma pin
   from the root `build.gradle.kts`, and the `/shots/`, `.kotlin/`,
   `node_modules/` lines in `.gitignore`. Commit `kotlin-js-store/`
   (the Yarn lockfile).
3. Copy `UiProbe.kt` and the bridge in `web/.../Main.kt`; give every
   tappable widget a `Modifier.probe("<tag>")`.
4. Adapt the **app-specific** parts of `tool/screenshot.mjs`: the seeded
   `localStorage` prefs (skip onboarding, mute audio, unlock levels) and the
   walk (which tags to tap, in what order). Run `--dump` to list tags.
5. Adjust the default `runs:` lines in the composite action to your screens.
6. AI review: copy `pr-review.yml` unchanged and add the `OPENROUTER_API_KEY`
   secret to that repo. Keep the guidelines in `AGENTS.md` (with a "Code
   review" section) and make `CLAUDE.md` a shim whose first line is
   `@AGENTS.md`.
7. One-time repo settings, in this order: Settings → Actions → General →
   Workflow permissions = "Read and write" so the preview and Pages jobs can
   push; then, once the first `pages.yml` or `pr-preview.yml` run has
   created the branch, Settings → Pages → Source = "Deploy from a branch",
   branch `gh-pages` / root. If the site already hosts static pages (this
   repo: the privacy policy in `docs/`), have `pages.yml` copy them into the
   deploy folder, otherwise their URLs go dead when the source changes.

## Gotchas worth remembering

- **Chromium vs proxy:** headless Chromium cannot reach `fonts.gstatic.com`
  through the egress proxy (Node can). Compose bundles its text font but
  fetches the *emoji* fallback from there; the script answers those requests
  from Node and caches them under `web/build/font-cache/`.
- **Locale:** the container's Chromium reports `en-US@posix`, which
  `Intl.Locale` rejects and Compose then throws on at boot. The script sets
  `locale: 'en-US'` on the browser context.
- **codeload.github.com is blocked:** Kotlin's Wasm tooling installs its
  karma fork from GitHub; the root build script pins the npm registry
  release (karma only runs browser unit tests, which this repo does not
  have).
- **JVM 64 KB method limit:** a single `listOf(...)` with all 60 levels blew
  it once Compose Multiplatform's compiler was in the loop; the generator
  emits one function per level.
- **Lazy grid:** off-screen level tiles are not composed and have no bounds;
  the script scrolls until the tile appears.
- **Autoplay:** browsers refuse audio before a user gesture; the web music
  player retries on the first pointer press.
