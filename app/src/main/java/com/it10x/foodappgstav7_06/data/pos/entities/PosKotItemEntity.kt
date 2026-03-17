package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_kot_items",
    indices = [
        Index(value = ["kotBatchId"]),
        Index(value = ["sessionId"]),   // ✅ MOST IMPORTANT
        Index(value = ["tableNo"]),
        Index(value = ["productId"]),
        Index(value = ["syncedToCloud"]), // ✅ fast sync lookup
        Index(value = ["source"])         // ✅ filter customer/waiter
    ]
)
data class PosKotItemEntity(

    @PrimaryKey
    val id: String,
    val categoryName: String,
    val sessionId: String?,             // groups table visit
    val kotBatchId: String,

    val tableNo: String?,               // UI / print only

    val productId: String,
    val name: String,
    val categoryId: String,

    val parentId: String?,
    val isVariant: Boolean,

    val basePrice: Double,
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,

    val status: String,                 // PENDING / DONE
    val note: String = "",
    val modifiersJson: String = "",

    val kitchenPrintReq: Boolean = true,
    val kitchenPrinted: Boolean = false,
    val createdAt: Long,

    // ================= NEW FIELDS =================

    val source: String = "POS",         // POS / CUSTOMER / WAITER

    val syncedToCloud: Boolean = false, // POS → Firestore mirror
    val syncedFromCloud: Boolean = false // Firestore → POS imported
)
