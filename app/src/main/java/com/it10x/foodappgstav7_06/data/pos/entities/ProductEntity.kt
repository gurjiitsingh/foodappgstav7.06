package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "products"
)
data class ProductEntity(

    // =====================================================
    // CORE IDENTITY (SYNC CRITICAL)
    // =====================================================
    @PrimaryKey
    val id: String,                // Firestore product ID

    // =====================================================
    // POS SEARCH (VERY IMPORTANT)
    // =====================================================
    val searchCode: String?,       // SKU / PLU / Barcode / Short Code

    // =====================================================
    // DISPLAY SNAPSHOT
    // =====================================================
    val name: String,
    val price: Double,
    val discountPrice: Double?,
    val image: String?,

    // =====================================================
// FOOD TYPE (NEW ⭐)
// =====================================================
    val foodType: String? = null,  // veg | non_veg | egg | vegan

    // =====================================================
    // SORTING (NEW ⭐)
    // =====================================================
    val sortOrder: Int = 0,        // lower = show first
    val kitchenPrintReq: Boolean? = null,
    // =====================================================
    // CATEGORY
    // =====================================================
    val categoryId: String,
    val productCat: String,

    // =====================================================
    // VARIANT RELATION (MOST IMPORTANT)
    // =====================================================
    val parentId: String?,         // NULL = main product
    val baseProductId: String?,    // keep for future compatibility
    val hasVariants: Boolean,

    // =====================================================
    // INVENTORY (READ ONLY IN POS)
    // =====================================================
    val stockQty: Int,

    // =====================================================
    // TAX (SNAPSHOT FROM WEB)
    // =====================================================
    val taxRate: Double?,
    val taxType: String?,          // inclusive | exclusive

    // =====================================================
    // OPTIONAL META (SAFE)
    // =====================================================
    val type: String? ,             // keep but DO NOT rely on it
    // ------------ OUTLET LINK ------------
    val outletId: String? = null
)




