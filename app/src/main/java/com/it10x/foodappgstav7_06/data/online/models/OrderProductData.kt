package com.it10x.foodappgstav7_06.data.online.models

data class OrderProductData(

    val id: String = "",
    val orderMasterId: String = "",

    val categoryName: String = "",
    val name: String = "",
    val quantity: Int = 0,

    val price: Any? = null,
    val itemSubtotal: Any? = null,
    val taxRate: Any? = null,
    val taxAmount: Any? = null,
    val taxTotal: Any? = null,

    val finalPrice: Any? = null,
    val finalTotal: Any? = null,

    val productCat: String = "",
    val note: String? = null,
    val modifiersJson: String? = null

) {
    fun priceDouble() = price.toDoubleSafe()
    fun finalPriceDouble() = finalPrice.toDoubleSafe()
    fun finalTotalDouble() = finalTotal.toDoubleSafe()
}

fun Any?.toDoubleSafe(): Double {
    return when (this) {
        is Number -> this.toDouble()
        is String -> this.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }
}
