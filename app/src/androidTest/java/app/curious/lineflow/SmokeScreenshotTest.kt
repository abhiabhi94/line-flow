package app.curious.lineflow

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.File
import org.junit.Assert.assertNotNull
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
        outputDir.mkdirs()
    }

    @Test
    fun walkThroughMainScreens() {
        // First launch shows the animated tutorial overlay.
        device.wait(Until.hasObject(By.textContains("Watch closely")), TIMEOUT_MS)
        snap("01_tutorial")

        // Tapping anywhere dismisses it and reveals the level list.
        device.click(device.displayWidth / 2, device.displayHeight / 2)
        assertNotNull("Level select did not appear", device.wait(Until.findObject(By.text("LINEFLOW")), TIMEOUT_MS))
        snap("02_level_select")

        // Open level 1.
        device.findObject(By.text("1")).click()
        assertNotNull("Game screen did not appear", device.wait(Until.findObject(By.text("Level 1")), TIMEOUT_MS))
        device.waitForIdle()
        snap("03_game_level_1")

        // Ask for a hint.
        device.findObject(By.text("💡"))?.click()
        device.waitForIdle()
        snap("04_game_level_1_hint")

        // Back to the level list, then into settings.
        device.findObject(By.text("←")).click()
        assertNotNull("Level select did not reappear", device.wait(Until.findObject(By.text("LINEFLOW")), TIMEOUT_MS))
        device.findObject(By.text("⚙️")).click()
        assertNotNull("Settings did not appear", device.wait(Until.findObject(By.text("Settings")), TIMEOUT_MS))
        snap("05_settings")
    }

    private fun snap(name: String) {
        device.takeScreenshot(File(outputDir, "$name.png"))
    }

    private companion object {
        const val TIMEOUT_MS = 15_000L
    }
}
