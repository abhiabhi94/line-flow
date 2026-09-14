package app.curious.lineflow

import android.content.Context
import android.content.SharedPreferences

/** Progress storage on Android: the same SharedPreferences file the app has always used. */
class SharedPreferencesStore(context: Context) : KeyValueStore {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(GameProgressRepository.STORE_NAME, Context.MODE_PRIVATE)

    override fun getStringSet(key: String): Set<String> =
        prefs.getStringSet(key, emptySet()).orEmpty()

    override fun putStringSet(key: String, value: Set<String>) {
        prefs.edit().putStringSet(key, value).apply()
    }

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)

    override fun putInt(key: String, value: Int) {
        prefs.edit().putInt(key, value).apply()
    }

    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)

    override fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }
}
