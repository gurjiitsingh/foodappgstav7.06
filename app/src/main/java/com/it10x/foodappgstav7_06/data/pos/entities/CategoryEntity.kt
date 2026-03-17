package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(

    @PrimaryKey
    val id: String,

    val name: String,
    val desc: String,
    val image: String?,

    // Tax (category default)
    val taxRate: Double?,
    val taxType: String?, // "inclusive" | "exclusive"

    // 🔮 FUTURE-PROOF FIELDS
    val sortOrder: Int = 0,
    val slug: String? = null,
    val isFeatured: Boolean = false,
    val kitchenPrintReq: Boolean? = null,
    // Sync safety
    val updatedAt: Long? = null,
    val isDeleted: Boolean = false,
    val outletId: String? = null
)




