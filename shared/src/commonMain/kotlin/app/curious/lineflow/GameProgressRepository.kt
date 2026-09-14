package app.curious.lineflow

class GameProgressRepository(private val store: KeyValueStore) {
    fun getCompletedLevelIds(): Set<Int> = store
        .getStringSet(KEY_COMPLETED_LEVELS)
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    fun markLevelCompleted(levelId: Int) {
        val current = store.getStringSet(KEY_COMPLETED_LEVELS).toMutableSet()
        current.add(levelId.toString())
        store.putStringSet(KEY_COMPLETED_LEVELS, current)
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        val completed = getCompletedLevelIds()
        return completed.contains(levelId - 1)
    }

    fun getHintsUsed(): Set<Int> = store
        .getStringSet(KEY_HINTS_USED)
        .mapNotNull { it.toIntOrNull() }
        .toSet()

    fun markHintUsed(levelId: Int) {
        val current = store.getStringSet(KEY_HINTS_USED).toMutableSet()
        current.add(levelId.toString())
        store.putStringSet(KEY_HINTS_USED, current)
    }

    fun markHintUsed(levelId: Int, depth: Int) {
        markHintUsed(levelId)
        val currentMax = getHintDepth(levelId)
        if (depth > currentMax) {
            store.putInt("$KEY_HINT_DEPTH_PREFIX$levelId", depth)
        }
    }

    fun getHintDepth(levelId: Int): Int = store.getInt("$KEY_HINT_DEPTH_PREFIX$levelId", 0)

    fun getLastPlayedLevelId(): Int = store.getInt(KEY_LAST_PLAYED, 1)

    fun setLastPlayedLevelId(levelId: Int) {
        store.putInt(KEY_LAST_PLAYED, levelId)
    }

    fun hasSeenTutorial(): Boolean = store.getBoolean(KEY_TUTORIAL_SEEN, false)

    fun markTutorialSeen() {
        store.putBoolean(KEY_TUTORIAL_SEEN, true)
    }

    fun isMusicEnabled(): Boolean = store.getBoolean(KEY_MUSIC_ENABLED, false)

    fun setMusicEnabled(enabled: Boolean) {
        store.putBoolean(KEY_MUSIC_ENABLED, enabled)
    }

    fun isVibrationEnabled(): Boolean = store.getBoolean(KEY_VIBRATION_ENABLED, true)

    fun setVibrationEnabled(enabled: Boolean) {
        store.putBoolean(KEY_VIBRATION_ENABLED, enabled)
    }

    companion object {
        /** SharedPreferences file on Android; key prefix in the browser. */
        const val STORE_NAME = "lineflow_progress"
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_HINTS_USED = "hints_used"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
        private const val KEY_LAST_PLAYED = "last_played_level"
        private const val KEY_HINT_DEPTH_PREFIX = "hint_depth_"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
}
