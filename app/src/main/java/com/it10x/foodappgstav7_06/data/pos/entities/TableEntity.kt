package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tables")
data class TableEntity(
    @PrimaryKey val id: String,

    /** Human-readable table name or number */
    val tableName: String,

    /** Current table status */
    val status: String, // AVAILABLE / OCCUPIED / BILL_REQUESTED / CLEANING / RESERVED

    /** Assigned waiter */
    val waiterName: String? = null,
    val waiterId: String? = null,

    /** Firestore active order */
    val activeOrderId: String? = null,

    /** Guest count */
    val guestsCount: Int? = null,

    /** Area grouping */
    val area: String? = null,

    /** Sort order */
    val sortOrder: Int? = null,

    /** 🔥 LIVE COUNTERS (UI cache layer) */
    val cartCount: Int = 0,
    val kitchenCount: Int = 0,
    val billCount: Int = 0,
    val billAmount: Double = 0.0,

    /** timestamps */
    val updatedAt: Long? = null,
    val createdAt: Long? = null,

    /** notes */
    val notes: String? = null,

    /** sync flag */
    val synced: Boolean? = null
)
