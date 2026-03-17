package com.it10x.foodappgstav7_06.data.pos.repository

import com.it10x.foodappgstav7_06.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity

class DeliverySettlementRepository(
    private val orderDao: OrderMasterDao
) {

    suspend fun getPendingDeliveries(): List<PosOrderMasterEntity> {
        return orderDao.getDeliveryPendingOrders()
    }

    suspend fun settleOrder(
        orderId: String,
        paymentMode: String,
        grandTotal: Double
    ) {
        orderDao.updatePaymentStatus(
            orderId = orderId,
            status = "PAID",
            paymentMode = paymentMode,
            paidAmount = grandTotal,
            dueAmount = 0.0,
            time = System.currentTimeMillis()
        )
    }
}