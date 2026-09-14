@AGENTS.md

## Claude Code

The repository guidelines live in `AGENTS.md` (imported above) so that Codex,
Claude Code, and other agents share one set of rules. Put new guidance there;
keep this file for Claude-only notes.

- Cloud sessions: `.claude/hooks/session-start.sh` installs the Android SDK
  pieces Gradle needs, warms the Gradle/Kotlin-Wasm toolchains and checks
  Playwright; `.claude/skills/run` drives the web-build screenshot harness
  for visual checks (there is no emulator in the container).
