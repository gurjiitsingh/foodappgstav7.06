package com.it10x.foodappgstav7_06.data.pos.entities


import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "pos_order_payments",
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["syncStatus"])
    ]
)
data class PosOrderPaymentEntity(

    @PrimaryKey
    val id: String,              // UUID

    val orderId: String,

    // =====================================================
    // OWNERSHIP (IMPORTANT FOR SYNC)
    // =====================================================
    val ownerId: String,
    val outletId: String,

    // =====================================================
    // PAYMENT DETAILS
    // =====================================================
    val amount: Double,
    val mode: String,            // CASH | CARD | UPI | ONLINE
    val provider: String?,       // RAZORPAY | STRIPE
    val method: String?,         // VISA | GPAY | PHONEPE

    val status: String,          // SUCCESS | FAILED | REFUNDED

    // =====================================================
    // DEVICE TRACE
    // =====================================================
    val deviceId: String,

    // =====================================================
    // TIMING
    // =====================================================
    val createdAt: Long,

    // =====================================================
    // SYNC CONTROL
    // =====================================================
    val syncStatus: String = "PENDING",
    val lastSyncedAt: Long? = null,

    // =====================================================
    // REVERSAL / VOID SUPPORT
    // =====================================================
    val isVoided: Boolean = false
)
