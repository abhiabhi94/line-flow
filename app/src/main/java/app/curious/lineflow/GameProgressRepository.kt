package app.curious.lineflow

import android.content.Context
import android.content.SharedPreferences

class GameProgressRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun getCompletedLevelIds(): Set<Int> {
        return prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun markLevelCompleted(levelId: Int) {
        val current = prefs.getStringSet(KEY_COMPLETED_LEVELS, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        current.add(levelId.toString())
        prefs.edit().putStringSet(KEY_COMPLETED_LEVELS, current).apply()
    }

    fun isLevelUnlocked(levelId: Int): Boolean {
        if (levelId == 1) return true
        val completed = getCompletedLevelIds()
        return completed.contains(levelId - 1)
    }

    fun getHintsUsed(): Set<Int> {
        return prefs.getStringSet(KEY_HINTS_USED, emptySet())
            ?.mapNotNull { it.toIntOrNull() }
            ?.toSet()
            ?: emptySet()
    }

    fun markHintUsed(levelId: Int) {
        val current = prefs.getStringSet(KEY_HINTS_USED, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        current.add(levelId.toString())
        prefs.edit().putStringSet(KEY_HINTS_USED, current).apply()
    }

    fun markHintUsed(levelId: Int, depth: Int) {
        markHintUsed(levelId)
        val currentMax = getHintDepth(levelId)
        if (depth > currentMax) {
            prefs.edit().putInt("$KEY_HINT_DEPTH_PREFIX$levelId", depth).apply()
        }
    }

    fun getHintDepth(levelId: Int): Int {
        return prefs.getInt("$KEY_HINT_DEPTH_PREFIX$levelId", 0)
    }

    fun hasSeenTutorial(): Boolean {
        return prefs.getBoolean(KEY_TUTORIAL_SEEN, false)
    }

    fun markTutorialSeen() {
        prefs.edit().putBoolean(KEY_TUTORIAL_SEEN, true).apply()
    }

    fun isMusicEnabled(): Boolean {
        return prefs.getBoolean(KEY_MUSIC_ENABLED, false)
    }

    fun setMusicEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply()
    }

    fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    companion object {
        private const val PREFS_NAME = "lineflow_progress"
        private const val KEY_COMPLETED_LEVELS = "completed_levels"
        private const val KEY_HINTS_USED = "hints_used"
        private const val KEY_TUTORIAL_SEEN = "tutorial_seen"
        private const val KEY_HINT_DEPTH_PREFIX = "hint_depth_"
        private const val KEY_MUSIC_ENABLED = "music_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
    }
}
