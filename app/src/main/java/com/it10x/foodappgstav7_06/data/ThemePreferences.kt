package com.it10x.foodappgstav7_06.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.it10x.foodappgstav7_06.ui.theme.PosThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore("theme_prefs")

class ThemePreferences(private val context: Context) {

    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val themeMode: Flow<String> =
        context.themeDataStore.data.map {
            it[THEME_MODE] ?: PosThemeMode.DARK.name
        }

    suspend fun setThemeMode(mode: String) {
        context.themeDataStore.edit {
            it[THEME_MODE] = mode
        }
    }
}
