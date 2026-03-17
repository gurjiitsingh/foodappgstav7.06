package com.it10x.foodappgstav7_06.data.pos.repository

import com.it10x.foodappgstav7_06.data.pos.dao.PosOrderPaymentDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderPaymentEntity

class POSPaymentRepository(
    private val paymentDao: PosOrderPaymentDao
) {

    // -----------------------------------------------------
    // INSERT PAYMENTS
    // -----------------------------------------------------
    suspend fun insertPayments(payments: List<PosOrderPaymentEntity>) {
        paymentDao.insertAll(payments)
    }



    // -----------------------------------------------------
    // CALCULATIONS
    // -----------------------------------------------------
    suspend fun getTotalPaid(orderId: String): Double {
        return paymentDao.getTotalPaidForOrder(orderId)
    }

    // -----------------------------------------------------
    // VOID / REVERSAL
    // -----------------------------------------------------
    suspend fun voidPayment(paymentId: String) {
        paymentDao.voidPayment(paymentId)
    }

    // -----------------------------------------------------
    // SYNC
    // -----------------------------------------------------
    suspend fun getPendingSync() =
        paymentDao.getPendingSync()

    suspend fun markSynced(paymentId: String, time: Long) {
        paymentDao.markSynced(paymentId, time)
    }
}
