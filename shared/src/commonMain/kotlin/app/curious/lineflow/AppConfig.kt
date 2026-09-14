package app.curious.lineflow

/** Switches the platform entry point sets before composing [App]. */
object AppConfig {
    /**
     * Every level playable regardless of progress. The Android debug build
     * turns this on (it is the "testing" install); the browser build leaves
     * it off and the screenshot harness seeds progress instead.
     */
    var unlockAllLevels: Boolean = false
}
