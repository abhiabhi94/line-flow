import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        outputModuleName.set("lineflow")
        browser {
            commonWebpackConfig {
                outputFileName = "lineflow.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.ui)
        }
    }
}

// The background music lives once, as an Android raw resource in :shared;
// the browser build serves the same file next to index.html.
tasks.named<ProcessResources>("wasmJsProcessResources") {
    from(rootProject.file("shared/src/androidMain/res/raw/serene_loop.mp3"))
}

ktlint {
    // Pre-existing findings are recorded in the baseline; new code must be clean.
    baseline.set(rootProject.file("config/ktlint/baseline-web.xml"))
}

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    baseline = rootProject.file("config/detekt/baseline-web.xml")
    source.setFrom(files("src"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/Graph.kt")
}
