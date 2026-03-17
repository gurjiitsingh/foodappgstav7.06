package com.it10x.foodappgstav7_06.data.online.models

data class Product(
    val id: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val stockQty: Int = 0,
    val discountPrice: Double? = null,
    val categoryId: String = "",
    val productCat: String? = null,
    val baseProductId: String = "",
    val productDesc: String = "",
    val sortOrder: Int = 0,
    val image: String = "",
    val isFeatured: Boolean = false,
    val purchaseSession: String? = null,
    val quantity: Int? = null,
    val flavors: Boolean = false,
    val status: String? = null,
    val taxRate: Double? = null,
    val taxType: String? = null,
    val parentId: String? = null,
    val hasVariants: Boolean? = null,
    val type: String? = null,
    val outletId: String? = null
)
