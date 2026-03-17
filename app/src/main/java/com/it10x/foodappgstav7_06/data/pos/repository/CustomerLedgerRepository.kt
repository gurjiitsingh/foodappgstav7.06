package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import androidx.room.withTransaction
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerDao
import com.it10x.foodappgstav7_06.data.pos.dao.PosCustomerLedgerDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity
import java.util.*

class CustomerLedgerRepository(
    private val db: AppDatabase
) {

    private val customerDao = db.posCustomerDao()
    private val ledgerDao = db.posCustomerLedgerDao()
    private val orderDao = db.orderMasterDao()
    // -----------------------------------------------------
    // ORDER DEBIT (CREDIT SALE)
    // -----------------------------------------------------
    suspend fun addOrderDebit(
        customerId: String,
        ownerId: String,
        outletId: String,
        orderId: String,
        amount: Double
    ) {
        db.withTransaction {

            val lastBalance = ledgerDao.getLastBalance(customerId) ?: 0.0
            val newBalance = lastBalance + amount

            ledgerDao.insert(
                PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    outletId = outletId,
                    customerId = customerId,
                    orderId = orderId,
                    paymentId = null,
                    type = "ORDER",
                    debitAmount = amount,
                    creditAmount = 0.0,
                    balanceAfter = newBalance,
                    note = "Order Credit",
                    createdAt = System.currentTimeMillis(),
                    deviceId = "POS",
                    syncStatus = "PENDING"
                )
            )

            customerDao.increaseDue(customerId, amount)
        }
    }

    // -----------------------------------------------------
    // PAYMENT CREDIT
    // -----------------------------------------------------
    suspend fun addPaymentCredit(
        customerId: String,
        ownerId: String,
        outletId: String,
        paymentId: String,
        amount: Double
    ) {
        db.withTransaction {

            val lastBalance = ledgerDao.getLastBalance(customerId) ?: 0.0
            val newBalance = (lastBalance - amount).coerceAtLeast(0.0)

            ledgerDao.insert(
                PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    outletId = outletId,
                    customerId = customerId,
                    orderId = null,
                    paymentId = paymentId,
                    type = "PAYMENT",
                    debitAmount = 0.0,
                    creditAmount = amount,
                    balanceAfter = newBalance,
                    note = "Settlement Payment",
                    createdAt = System.currentTimeMillis(),
                    deviceId = "POS",
                    syncStatus = "PENDING"
                )
            )

            customerDao.decreaseDue(customerId, amount)
        }
    }


    suspend fun settleCustomerPayment(
        customerId: String,
        ownerId: String,
        outletId: String,
        paymentId: String,
        amount: Double,
        paymentMode: String
    ) {

        db.withTransaction {

            val now = System.currentTimeMillis()

            // 1️⃣ Get last balance
            val lastBalance = ledgerDao.getLastBalance(customerId) ?: 0.0
            val newBalance = (lastBalance - amount).coerceAtLeast(0.0)

            // 2️⃣ Insert ledger entry
            ledgerDao.insert(
                PosCustomerLedgerEntity(
                    id = UUID.randomUUID().toString(),
                    ownerId = ownerId,
                    outletId = outletId,
                    customerId = customerId,
                    orderId = null,
                    paymentId = paymentId,
                    type = "PAYMENT",
                    debitAmount = 0.0,
                    creditAmount = amount,
                    balanceAfter = newBalance,
                    note = "Settlement via $paymentMode",
                    createdAt = now,
                    deviceId = "POS",
                    syncStatus = "PENDING"
                )
            )

            // 3️⃣ Decrease customer due
            customerDao.decreaseDue(customerId, amount)

            // 4️⃣ 🔥 STEP 3 CODE GOES HERE 🔥
            val orders = orderDao.getPendingOrdersForCustomer(customerId)

            var remainingAmount = amount
            var updateCount = 0

            for (order in orders) {

                if (remainingAmount <= 0) break

                val due = order.dueAmount

                if (remainingAmount >= due) {

                    // Fully close order
                    orderDao.updatePaymentStatus(
                        orderId = order.id,
                        status = "PAID",
                        paymentMode = paymentMode,
                        paidAmount = order.grandTotal,
                        dueAmount = 0.0,
                        time = now
                    )

                    remainingAmount -= due
                    updateCount++

                } else {

                    // Partial close
                    val newDue = due - remainingAmount

                    orderDao.updatePaymentStatus(
                        orderId = order.id,
                        status = "PARTIAL",
                        paymentMode = paymentMode,
                        paidAmount = order.paidAmount + remainingAmount,
                        dueAmount = newDue,
                        time = now
                    )

                    remainingAmount = 0.0
                    updateCount++
                }
            }

            Log.d("SETTLEMENT", "Orders updated: $updateCount")
        }
    }


    suspend fun getLedger(customerId: String) =
        ledgerDao.getLedgerForCustomer(customerId)

    suspend fun getPendingSync() =
        ledgerDao.getPendingSync()

    suspend fun markSynced(id: String, time: Long) =
        ledgerDao.markSynced(id, time)

    suspend fun getAllCustomers() =
        customerDao.getAllCustomers()

    suspend fun getCustomerById(id: String) =
        customerDao.getCustomerById(id)
}
