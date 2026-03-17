package com.it10x.foodappgstav7_06.ui.delivery

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*


class DeliverySettlementViewModel(
    private val db: AppDatabase
) : ViewModel() {

    private val orderDao = db.orderMasterDao()
    private val customerDao = db.posCustomerDao()
    private val ledgerDao = db.posCustomerLedgerDao()

    private val _pendingOrders =
        MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())
    val pendingOrders: StateFlow<List<PosOrderMasterEntity>> = _pendingOrders

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _pendingOrders.value =
                orderDao.getDeliveryPendingOrders()
        }
    }

    fun markCollected(orderId: String, mode: String) {

        viewModelScope.launch {

            Log.d("DELIVERY_DEBUG", "MarkCollected called for orderId=$orderId")

            val order = orderDao.getOrderById(orderId)

            if (order == null) {
                Log.e("DELIVERY_DEBUG", "Order not found")
                return@launch
            }

            Log.d("DELIVERY_DEBUG", "Order found. grandTotal=${order.grandTotal}")

            orderDao.updatePaymentStatus(
                orderId = orderId,
                status = "PAID",
                paymentMode = mode,
                paidAmount = order.grandTotal,
                dueAmount = 0.0,
                time = System.currentTimeMillis()
            )

            Log.d("DELIVERY_DEBUG", "Update query executed")

            val updated = orderDao.getOrderById(orderId)

            Log.d("DELIVERY_DEBUG", """
            After Update:
            paymentStatus=${updated?.paymentStatus}
            paymentMode=${updated?.paymentMode}
            paidAmount=${updated?.paidAmount}
            dueAmount=${updated?.dueAmount}
        """.trimIndent())

            load()
        }
    }


    fun markNotCollected(orderId: String) {
        viewModelScope.launch {

            val order =
                orderDao.getOrderById(orderId) ?: return@launch

            val now = System.currentTimeMillis()

            customerDao.increaseDue(
                order.customerPhone!!,
                order.grandTotal
            )

            val lastBalance =
                ledgerDao.getLastBalance(order.customerPhone) ?: 0.0

            val newBalance = lastBalance + order.grandTotal

            ledgerDao.insert(
                PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = "",
                    outletId = "",
                    customerId = order.customerPhone,
                    orderId = order.id,
                    paymentId = null,
                    type = "ORDER",
                    debitAmount = order.grandTotal,
                    creditAmount = 0.0,
                    balanceAfter = newBalance,
                    note = "Delivery not collected",
                    createdAt = now,
                    deviceId = "POS"
                )
            )

            orderDao.updatePaymentStatus(
                orderId = orderId,
                status = "CREDIT",
                paymentMode = "DELIVERY_FAILED",
                paidAmount = 0.0,
                dueAmount = order.grandTotal, // pass real amount
                time = System.currentTimeMillis()
            )

            load()
        }
    }
}
