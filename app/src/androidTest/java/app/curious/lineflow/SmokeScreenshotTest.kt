package app.curious.lineflow

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import java.util.regex.Pattern
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Walks through the main screens on a real Android runtime and saves a
 * screenshot of each one, so the UI can be inspected from CI artifacts.
 *
 * Screenshots go to the directory AGP passes as `additionalTestOutputDir`
 * (collected into `app/build/outputs/connected_android_test_additional_output`)
 * or, failing that, the app's external files directory.
 */
@RunWith(AndroidJUnit4::class)
class SmokeScreenshotTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    private lateinit var device: UiDevice
    private lateinit var outputDir: File

    @Before
    fun setUp() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
        val argDir = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        outputDir =
            if (argDir != null) {
                File(argDir)
            } else {
                File(instrumentation.targetContext.getExternalFilesDir(null), "screenshots")
            }
        check(outputDir.isDirectory || outputDir.mkdirs()) { "Could not create $outputDir" }
    }

    @Test
    fun walkThroughMainScreens() {
        // First launch shows the animated tutorial overlay. With animations
        // disabled it finishes almost immediately, so it may already be gone.
        if (device.wait(Until.hasObject(By.text(TUTORIAL_TEXT)), SHORT_TIMEOUT_MS)) {
            snap("01_tutorial")
            // Tapping anywhere dismisses it; aim at empty space below the status bar.
            device.click(device.displayWidth / 2, device.displayHeight / 12)
        }
        assertNotNull("Level select did not appear", device.wait(Until.findObject(By.text("LINEFLOW")), TIMEOUT_MS))
        device.waitForIdle()
        snap("02_level_select")

        // Open level 1.
        tap("1")
        assertNotNull("Game screen did not appear", device.wait(Until.findObject(By.text("Level 1")), TIMEOUT_MS))
        device.waitForIdle()
        snap("03_game_level_1")

        // Ask for a hint; the first hint for level 1 explains it is a circuit.
        tap("💡")
        assertNotNull(
            "Hint text did not appear",
            device.wait(Until.findObject(By.textStartsWith("This is a circuit")), TIMEOUT_MS),
        )
        snap("04_game_level_1_hint")

        // Back to the level list, then into settings.
        tap("←")
        assertNotNull("Level select did not reappear", device.wait(Until.findObject(By.text("LINEFLOW")), TIMEOUT_MS))
        tap("⚙️")
        assertNotNull("Settings did not appear", device.wait(Until.findObject(By.text("Settings")), TIMEOUT_MS))
        snap("05_settings")
    }

    /** Waits for an element with the given text to appear, then clicks it. */
    private fun tap(text: String) {
        val target = device.wait(Until.findObject(By.text(text)), TIMEOUT_MS)
        assertNotNull("No element with text \"$text\" to tap", target)
        target.click()
    }

    private fun snap(name: String) {
        val file = File(outputDir, "$name.png")
        assertTrue("Failed to capture screenshot $file", device.takeScreenshot(file))
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
        const val SHORT_TIMEOUT_MS = 2_000L
        val TUTORIAL_TEXT: Pattern = Pattern.compile("Watch closely.*|Don't .*|Trace every line.*")
    }
}
