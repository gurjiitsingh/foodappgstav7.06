package com.it10x.foodappgstav7_06.ui.orders.local

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.viewmodel.POSOrdersViewModel
import com.it10x.foodappgstav7_06.printer.PrinterManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.it10x.foodappgstav7_06.printer.PosReceiptBuilder
class LocalOrderDetailViewModel(
    private val orderId: String,
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {

    private val _orderInfo = MutableStateFlow<PosOrderMasterEntity?>(null)
    val orderInfo: StateFlow<PosOrderMasterEntity?> = _orderInfo

    val products: StateFlow<List<PosOrderItemEntity>> =
        flow { emit(repository.getOrderItems(orderId)) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val subtotal = products.map { it.sumOf { p -> p.itemSubtotal } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)


    val discount = orderInfo
        .map { it?.discountTotal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val taxTotal = orderInfo
        .map { it?.taxTotal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val grandTotal = orderInfo
        .map { it?.grandTotal ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val totalPaid = orderInfo
        .map { it?.paidAmount ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val dueAmount = combine(grandTotal, totalPaid) { total, paid ->
        (total - paid).coerceAtLeast(0.0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)

    val paymentStatus = combine(totalPaid, dueAmount) { paid, due ->
        when {
            paid == 0.0 -> "CREDIT"
            due > 0.0 -> "PARTIAL"
            else -> "PAID"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "CREDIT")


    init {
        viewModelScope.launch {
            _orderInfo.value = repository.getOrderById(orderId)
        }
    }

    fun updateGrandTotal(newTotal: Double) {
        val current = _orderInfo.value ?: return
        viewModelScope.launch {
            repository.updateGrandTotal(current.id, newTotal)

            // ✅ update local state immediately for UI refresh
            _orderInfo.value = current.copy(grandTotal = newTotal, updatedAt = System.currentTimeMillis())
        }
    }
}
