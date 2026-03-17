package com.it10x.foodappgstav7_06.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CustomerLedgerRepository

class CustomerStatementViewModel(
    private val customerRepository: CustomerRepository,
    private val ledgerRepository: CustomerLedgerRepository
) : ViewModel() {

    var customer by mutableStateOf<PosCustomerEntity?>(null)
        private set

    var ledger by mutableStateOf<List<PosCustomerLedgerEntity>>(emptyList())
        private set

    fun load(customerId: String) {
        viewModelScope.launch {
            customer = customerRepository.getById(customerId)
            ledger = ledgerRepository.getLedger(customerId)
        }
    }
}
