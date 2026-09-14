pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    // Default mode (PREFER_PROJECT): the Kotlin/Wasm toolchain registers the Node and
    // Yarn download repositories on the root project, which FAIL_ON_PROJECT_REPOS rejects.
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
    }
}

rootProject.name = "LineFlow"

// shared: the whole game (Kotlin Multiplatform + Compose Multiplatform).
// app:    the Android application (thin: MainActivity + launcher resources).
// web:    the browser build (Kotlin/Wasm), used for GitHub Pages and for
//         visual verification in cloud sessions and CI.
include(":shared")
include(":app")
include(":web")
