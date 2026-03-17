package com.it10x.foodappgstav7_06.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.printer.PrinterManager

class OnlineOrdersViewModelFactory(
    private val printerManager: PrinterManager // only inject dependencies
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnlineOrdersViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnlineOrdersViewModel(printerManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

