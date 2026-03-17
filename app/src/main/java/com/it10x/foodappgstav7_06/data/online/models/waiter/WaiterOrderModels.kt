package com.it10x.foodappgstav7_06.data.online.models.waiter

data class WaiterOrder(
    val orderId: String = "",
    val tableNo: String = "",
    val sessionId: String = "",
    val orderType: String = "",
    val deviceId: String = "",
    val deviceName: String? = null,
    val status: String = "PENDING", // PENDING, ACCEPTED, BILLED
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

data class WaiterOrderItem(
    val productId: String = "",
    val productName: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val quantity: Int = 0,
    val price: Double = 0.0,
    val taxRate: Double = 0.0,
    val tableNo: String = "",
    val sessionId: String = "",
    val note : String = "",
    val modifiersJson : String = "",
    val kitchenPrintReq: Boolean = true,
    val kitchenPrinted: Boolean = false
)
