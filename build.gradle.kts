// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.kmp.library) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.compose.multiplatform) apply false
    alias(libs.plugins.ktlint) apply false
    alias(libs.plugins.detekt) apply false
}

// The Kotlin/Wasm tooling installs its own karma fork straight from GitHub
// ("github:Kotlin/karma#..."), which cloud dev sessions cannot fetch (the
// egress proxy blocks codeload.github.com). Karma only runs browser unit
// tests, which this project does not have; the registry release installs
// from registry.npmjs.org everywhere and keeps the tooling setup working.
plugins.withType<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootPlugin> {
    the<org.jetbrains.kotlin.gradle.targets.wasm.nodejs.WasmNodeJsRootExtension>().versions.karma.version = "6.4.4"
}
