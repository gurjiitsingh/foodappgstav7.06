package com.it10x.foodappgstav7_06.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.printer.PrinterManager

class PrinterSettingsViewModelFactory(
    private val prefs: PrinterPreferences,
    private val printerManager: PrinterManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrinterSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrinterSettingsViewModel(prefs, printerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
