package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "pos_customer_ledger",
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["orderId"]),
        Index(value = ["createdAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class PosCustomerLedgerEntity(

    // =====================================================
    // CORE IDENTITY
    // =====================================================
    @PrimaryKey
    val id: String,                  // UUID

    val ownerId: String,
    val outletId: String,

    val customerId: String,

    // =====================================================
    // REFERENCE
    // =====================================================
    val orderId: String?,            // Null if settlement
    val paymentId: String?,          // Null if order entry

    // =====================================================
    // TRANSACTION TYPE
    // =====================================================
    val type: String,                // ORDER | PAYMENT | REFUND | ADJUSTMENT

    // =====================================================
    // AMOUNTS
    // =====================================================
    val debitAmount: Double,         // Increases due
    val creditAmount: Double,        // Reduces due

    val balanceAfter: Double,        // Running balance after this entry

    // =====================================================
    // NOTES
    // =====================================================
    val note: String? = null,

    // =====================================================
    // TIMING
    // =====================================================
    val createdAt: Long,

    // =====================================================
    // DEVICE TRACE
    // =====================================================
    val deviceId: String,

    // =====================================================
    // SYNC CONTROL
    // =====================================================
    val syncStatus: String = "PENDING",
    val lastSyncedAt: Long? = null
)
