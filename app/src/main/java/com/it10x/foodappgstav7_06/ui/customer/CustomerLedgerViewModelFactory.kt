package com.it10x.foodappgstav7_06.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerLedgerRepository

class CustomerLedgerViewModelFactory(
    private val repository: CustomerLedgerRepository,
    private val customerId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CustomerLedgerViewModel(repository, customerId) as T
    }
}
