package app.curious.lineflow

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // The debug build type appends ".debug" to the application id.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val expected = setOf("app.curious.lineflow", "app.curious.lineflow.debug")
        assertTrue("Unexpected package ${appContext.packageName}", appContext.packageName in expected)
    }
}
