#!/bin/bash
# SessionStart hook for Claude Code on the web (cloud sessions only).
#
# Installs the Android SDK pieces Gradle needs to configure the project
# (there is no emulator: the container has no KVM), warms the Gradle and
# Kotlin/Wasm toolchains, and makes sure the headless-Chromium screenshot
# harness (tool/screenshot.mjs) can run. Idempotent: the container state is
# cached after the hook completes, so re-runs are quick.
set -euo pipefail

if [ "${CLAUDE_CODE_REMOTE:-}" != "true" ]; then
  exit 0
fi

PROJECT_DIR="${CLAUDE_PROJECT_DIR:-$(cd "$(dirname "$0")/../.." && pwd)}"
ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
CMDLINE_TOOLS_ZIP="https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip"
# Keep in sync with compileSdk in app/build.gradle.kts and shared/build.gradle.kts
# (Google names the SDK 37 package "android-37.0"; AGP resolves compileSdk = 37 to it).
ANDROID_PLATFORM="platforms;android-37.0"
BUILD_TOOLS="build-tools;36.0.0"

# --- Android SDK -----------------------------------------------------------
SDKMANAGER="$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager"
if [ ! -x "$SDKMANAGER" ]; then
  echo "[session-start] Installing Android command-line tools to $ANDROID_HOME"
  mkdir -p "$ANDROID_HOME/cmdline-tools"
  tmp="$(mktemp -d)"
  curl -fsSL -o "$tmp/tools.zip" "$CMDLINE_TOOLS_ZIP"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  rm -rf "$ANDROID_HOME/cmdline-tools/latest"
  mv "$tmp/cmdline-tools" "$ANDROID_HOME/cmdline-tools/latest"
  rm -rf "$tmp"
fi
if [ ! -d "$ANDROID_HOME/platforms/${ANDROID_PLATFORM#platforms;}" ] || [ ! -d "$ANDROID_HOME/build-tools/${BUILD_TOOLS#build-tools;}" ]; then
  echo "[session-start] Installing $ANDROID_PLATFORM, $BUILD_TOOLS, platform-tools"
  yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
  "$SDKMANAGER" --install "platform-tools" "$ANDROID_PLATFORM" "$BUILD_TOOLS" >/dev/null
fi
export ANDROID_HOME
if [ ! -f "$PROJECT_DIR/local.properties" ]; then
  echo "sdk.dir=$ANDROID_HOME" > "$PROJECT_DIR/local.properties"
fi

# --- Gradle / Kotlin / Wasm toolchains -------------------------------------
# One configuration pass downloads Gradle, the plugins, Node and Yarn (the
# Kotlin/Wasm tooling). The karma fork the tooling would fetch from GitHub is
# swapped for the npm registry release in build.gradle.kts, because the cloud
# egress proxy blocks codeload.github.com.
cd "$PROJECT_DIR"
echo "[session-start] Warming Gradle (compiling shared for JVM + Wasm tooling)"
./gradlew --quiet :shared:compileKotlinJvm :kotlinWasmToolingSetup >/dev/null 2>&1 || \
  ./gradlew --quiet :shared:compileKotlinJvm

# --- Screenshot harness (Playwright + Chromium) ----------------------------
# The web image ships playwright + a matching Chromium under /opt/pw-browsers;
# install them only if missing so the hook also works on a bare image.
NODE_GLOBAL="$(npm root -g)"
if ! NODE_PATH="$NODE_GLOBAL" node -e "require('playwright')" >/dev/null 2>&1; then
  echo "[session-start] Installing playwright"
  npm install -g playwright@1.56.1 >/dev/null
fi
if [ -z "${PLAYWRIGHT_BROWSERS_PATH:-}" ] || [ ! -d "$PLAYWRIGHT_BROWSERS_PATH" ]; then
  echo "[session-start] Installing Chromium for playwright"
  NODE_PATH="$NODE_GLOBAL" npx playwright install chromium >/dev/null
fi

# --- Persist env for the session ------------------------------------------
if [ -n "${CLAUDE_ENV_FILE:-}" ]; then
  {
    echo "export ANDROID_HOME=\"$ANDROID_HOME\""
    echo "export NODE_PATH=\"$NODE_GLOBAL\""
  } >> "$CLAUDE_ENV_FILE"
fi

echo "[session-start] Ready. Screenshots: ./gradlew :web:wasmJsBrowserDistribution && node tool/screenshot.mjs --levels 1,20 --settings"
