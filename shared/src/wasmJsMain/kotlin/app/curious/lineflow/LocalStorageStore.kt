// Parameters of the js() functions are read by the JavaScript snippets.
@file:Suppress("UnusedParameter")

package app.curious.lineflow

private fun storageGet(key: String): String? = js("localStorage.getItem(key)")

private fun storageSet(key: String, value: String): Unit = js("localStorage.setItem(key, value)")

/**
 * Progress storage in the browser: one localStorage entry per key, prefixed
 * with the store name. Sets are stored newline-separated (values are level
 * ids, so the separator can never collide).
 */
class LocalStorageStore : KeyValueStore {
    private fun name(key: String) = "${GameProgressRepository.STORE_NAME}.$key"

    override fun getStringSet(key: String): Set<String> = storageGet(name(key))
        ?.split(SET_SEPARATOR)
        ?.filter { it.isNotEmpty() }
        ?.toSet()
        .orEmpty()

    override fun putStringSet(key: String, value: Set<String>) {
        storageSet(name(key), value.joinToString(SET_SEPARATOR))
    }

    override fun getInt(key: String, default: Int): Int =
        storageGet(name(key))?.toIntOrNull() ?: default

    override fun putInt(key: String, value: Int) {
        storageSet(name(key), value.toString())
    }

    override fun getBoolean(key: String, default: Boolean): Boolean =
        storageGet(name(key))?.toBooleanStrictOrNull() ?: default

    override fun putBoolean(key: String, value: Boolean) {
        storageSet(name(key), value.toString())
    }

    private companion object {
        const val SET_SEPARATOR = "\n"
    }
}
