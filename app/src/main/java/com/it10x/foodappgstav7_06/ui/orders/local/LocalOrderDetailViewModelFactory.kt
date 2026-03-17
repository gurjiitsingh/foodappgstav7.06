package com.it10x.foodappgstav7_06.ui.orders.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_06.printer.PrinterManager

import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository

class LocalOrderDetailViewModelFactory(
    private val orderId: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalOrderDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocalOrderDetailViewModel(
                orderId = orderId,
                repository = repository,
                printerManager = printerManager
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
