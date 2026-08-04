package com.pichler.digitaleshirn.data

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Stores and retrieves custom display names for each internal [Category].
 * Falls back to [Category.defaultName] when no custom name is set.
 */
class CategorySettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the visible name for [category], falling back to its default. */
    fun getDisplayName(category: Category): String =
        prefs.getString(prefKey(category), null)?.takeIf { it.isNotBlank() }
            ?: category.defaultName

    /** Saves a custom display name for [category]. Blank values are ignored. */
    fun setDisplayName(category: Category, name: String) {
        if (name.isNotBlank()) {
            prefs.edit { putString(prefKey(category), name.trim()) }
        }
    }

    /** Resets all category names to their defaults. */
    fun resetToDefaults() {
        prefs.edit {
            Category.entries.forEach { remove(prefKey(it)) }
        }
    }

    /** Returns a map of all display names (current custom or default). */
    fun getAllDisplayNames(): Map<Category, String> =
        Category.entries.associateWith { getDisplayName(it) }

    /**
     * Builds the keyword → category mapping used for voice classification.
     * Includes default keywords AND any custom names the user has defined.
     */
    fun buildKeywordMap(): Map<String, Category> {
        val map = Category.defaultKeywords.toMutableMap()
        Category.entries.forEach { cat ->
            val custom = prefs.getString(prefKey(cat), null)?.trim()?.lowercase()
            if (!custom.isNullOrBlank()) {
                map[custom] = cat
            }
        }
        return map
    }

    private fun prefKey(category: Category) = "cat_name_${category.name}"

    companion object {
        private const val PREFS_NAME = "category_settings"

        @Volatile
        private var INSTANCE: CategorySettingsRepository? = null

        fun getInstance(context: Context): CategorySettingsRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CategorySettingsRepository(context.applicationContext).also {
                    INSTANCE = it
                }
            }
    }
}
