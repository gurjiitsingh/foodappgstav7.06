package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(
    tableName = "virtual_tables",
    indices = [
        Index(value = ["orderType"]),
        Index(value = ["status"]),
        Index(value = ["activeOrderId"])
    ]
)
data class VirtualTableEntity(

    @PrimaryKey
    val id: String, // 🔥 MUST be same as order.id

    /** Human-readable name like TA-101 / DL-205 */
    val tableName: String,

    /** TAKEAWAY or DELIVERY */
    val orderType: String, // TAKEAWAY / DELIVERY

    /** Current status */
    val status: String, // AVAILABLE / OCCUPIED / BILL_REQUESTED / COMPLETED

    /** Assigned waiter (optional for takeaway) */
    val waiterName: String? = null,
    val waiterId: String? = null,

    /** Linked order */
    val activeOrderId: String? = null,

    /** Guest count (optional) */
    val guestsCount: Int? = null,

    /** Area grouping (optional, like FRONT_COUNTER / DELIVERY) */
    val area: String? = null,

    /** Sort order for UI */
    val sortOrder: Int? = null,

    /** 🔥 LIVE COUNTERS */
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