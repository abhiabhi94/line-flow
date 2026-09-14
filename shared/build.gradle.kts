import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kmp.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.detekt)
}

kotlin {
    // Host JVM target: runs the level validation tests (`./gradlew jvmTest`)
    // without an emulator or a browser.
    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    androidLibrary {
        namespace = "app.curious.lineflow.shared"
        compileSdk = 37
        minSdk = 24

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            // res/raw/serene_loop.mp3 for the Android MediaPlayer.
            enable = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(libs.compose.ui.backhandler)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.ktx)
        }
        jvmTest.dependencies {
            implementation(kotlin("test"))
            implementation(kotlin("test-junit"))
            implementation(libs.junit)
        }
    }
}

ktlint {
    // Pre-existing findings are recorded in the baseline; new code must be clean.
    baseline.set(rootProject.file("config/ktlint/baseline-shared.xml"))
}

detekt {
    config.setFrom(rootProject.file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    baseline = rootProject.file("config/detekt/baseline-shared.xml")
    source.setFrom(files("src"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    exclude("**/Graph.kt")
}
