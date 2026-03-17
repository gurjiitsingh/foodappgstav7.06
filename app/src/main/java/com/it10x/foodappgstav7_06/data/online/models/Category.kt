package com.it10x.foodappgstav7_06.data.online.models

data class Category(
    val id: String = "",
    val name: String = "",
    val desc: String = "",
    val productDesc: String? = null,
    val slug: String? = null,
    val image: String? = null,
    val isFeatured: Boolean? = null,
    val sortOrder: Int? = null,
    val disablePickupDiscount: Boolean? = null,
    val taxRate: Double? = null,
    val taxType: String? = null,
    val outletId: String? = null
)
