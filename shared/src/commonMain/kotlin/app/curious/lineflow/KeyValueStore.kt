package app.curious.lineflow

/**
 * The few primitive operations progress persistence needs. Android backs it
 * with SharedPreferences (keeping the file existing players already have);
 * the browser build backs it with localStorage.
 */
interface KeyValueStore {
    fun getStringSet(key: String): Set<String>

    fun putStringSet(key: String, value: Set<String>)

    fun getInt(key: String, default: Int): Int

    fun putInt(key: String, value: Int)

    fun getBoolean(key: String, default: Boolean): Boolean

    fun putBoolean(key: String, value: Boolean)
}
