package com.it10x.foodappgstav7_06.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.ThemePreferences
import com.it10x.foodappgstav7_06.ui.theme.PosThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = ThemePreferences(app)

    val themeMode = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        PosThemeMode.DARK.name
    )

    fun setThemeMode(mode: PosThemeMode) = viewModelScope.launch {
        prefs.setThemeMode(mode.name)
    }
}
