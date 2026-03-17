package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cart",
    indices = [
        // Allow same product multiple times IF note or modifiers differ
        Index(
            value = ["productId", "tableId", "note", "modifiersJson"],
            unique = true
        )
    ]
)
data class PosCartEntity(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val productId: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val parentId: String?,
    val isVariant: Boolean,

    val basePrice: Double,
    val quantity: Int,

    val taxRate: Double,
    val taxType: String,

    // 🔑 POS SESSION
    val sessionId: String,

    // 🪑 Only for DINE_IN
    val tableId: String?,

    // 📝 Free text kitchen instruction
    val note: String = "",

    // ⭐ Structured modifiers (future support: size, toppings, addons)
    // Store as JSON string
    val modifiersJson: String = "",

    // 🚀 Kitchen workflow
    val sentToKitchen: Boolean = false,
    var kitchenPrintReq: Boolean = false,
    var printStatus: String = "PENDING",

    val createdAt: Long = System.currentTimeMillis()
)
