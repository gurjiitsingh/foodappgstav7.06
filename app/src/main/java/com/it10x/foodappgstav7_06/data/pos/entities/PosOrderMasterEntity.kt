package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
@Entity(
    tableName = "pos_order_master",
    indices = [
        Index(value = ["syncStatus"]),
        Index(value = ["createdAt"]),
        Index(value = ["orderType"])
    ]
)
data class PosOrderMasterEntity(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================
    @PrimaryKey
    val id: String,                 // UUID (generated on POS)

    @ColumnInfo(name = "srno")
    val srno: Int,                  // Daily running number (POS)
    val orderType: String,          // DINE_IN | TAKEAWAY | DELIVERY | ONLINE
    val tableNo: String?,           // Only for DINE_IN

    // =====================================================
    // CUSTOMER (SNAPSHOT FOR PRINTING)
    // =====================================================cd  ..

    val customerName: String? = null,
    val customerPhone: String? = null,
    val customerId: String? = null,
    // =====================================================
    // DELIVERY ADDRESS SNAPSHOT (ONLY FOR DELIVERY / ONLINE)
    // =====================================================
    var dAddressLine1: String? = null,
    var dAddressLine2: String? = null,
    var dCity: String? = null,
    var dState: String? = null,
    var dZipcode: String? = null,
    var dLandmark: String? = null,


    // =====================================================
    // AMOUNTS (FINAL VALUES ONLY)
    // =====================================================
    val deliveryFee: Double = 0.0,
    val itemTotal: Double,          // Sum of items before tax
    val taxTotal: Double,           // Total tax
    val discountTotal: Double,      // Manual / item discount
    val grandTotal: Double,         // Final payable amount

    // =====================================================
    // PAYMENT
    // =====================================================

    val paymentMode: String,        // CASH | CARD | UPI | CREDIT | MIXED

    val paymentStatus: String,      // PAID | PARTIAL | CREDIT

    val paidAmount: Double = 0.0,   // Amount received now
    val dueAmount: Double = 0.0,    // Remaining amount

    // =====================================================
    // ORDER STATE
    // =====================================================
    val orderStatus: String,        // NEW | ACCEPTED | COMPLETED | CANCELLED

    // =====================================================
    // SOURCE & DEVICE META (SYNC CRITICAL)
    // =====================================================
    val source: String = "POS",     // POS | WEB | APP
    val deviceId: String,           // Unique per device
    val deviceName: String?,        // Optional (Tablet-1)
    val appVersion: String?,        // App version used

    // =====================================================
    // TIMING
    // =====================================================
    val createdAt: Long,            // System.currentTimeMillis()
    val updatedAt: Long?,           // Last local update

    // =====================================================
    // SYNC CONTROL
    // =====================================================
    val syncStatus: String,         // PENDING | SYNCED | FAILED
    val lastSyncedAt: Long?,        // Timestamp after successful sync

    // =====================================================
    // EXTRA (SAFE EXTENSION)
    // =====================================================
    val notes: String?              // Optional POS notes
)
