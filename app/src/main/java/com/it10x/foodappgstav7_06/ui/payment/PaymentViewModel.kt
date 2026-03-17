package com.it10x.foodappgstav7_06.ui.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PaymentViewModel(
    private val repository: POSOrdersRepository,
    private val tableId: String
) : ViewModel() {

    private val _paymentDone = MutableStateFlow(false)
    val paymentDone: StateFlow<Boolean> = _paymentDone

    fun confirmPayment(type: PaymentType) {
        viewModelScope.launch {

            // 1️⃣ Mark all orders PAID
            repository.markOrdersPaid(
                tableNo = tableId,
                paymentType = type.name
            )



            // 3️⃣ Notify UI
            _paymentDone.value = true
        }
    }
}
