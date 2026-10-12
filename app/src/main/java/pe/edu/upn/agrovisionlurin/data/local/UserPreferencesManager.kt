package pe.edu.upn.agrovisionlurin.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestiona la sesión del usuario local usando SharedPreferences.
 * Diseño offline-first: no requiere conexión a red para autenticación.
 */
class UserPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val loggedInUsername: String
        get() = prefs.getString(KEY_USERNAME, "") ?: ""

    val loggedInDisplayName: String
        get() = prefs.getString(KEY_DISPLAY_NAME, "") ?: ""

    val loggedInRole: String
        get() = prefs.getString(KEY_ROLE, "") ?: ""

    fun saveSession(username: String, displayName: String, role: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USERNAME, username)
            .putString(KEY_DISPLAY_NAME, displayName)
            .putString(KEY_ROLE, role)
            .apply()
    }

    fun clearSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USERNAME)
            .remove(KEY_DISPLAY_NAME)
            .remove(KEY_ROLE)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "agrovision_user_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "username"
        private const val KEY_DISPLAY_NAME = "display_name"
        private const val KEY_ROLE = "role"

        @Volatile
        private var INSTANCE: UserPreferencesManager? = null

        fun getInstance(context: Context): UserPreferencesManager {
            return INSTANCE ?: synchronized(this) {
                UserPreferencesManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
