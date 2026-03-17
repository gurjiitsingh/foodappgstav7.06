package com.it10x.foodappgstav7_06.data.mapper

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData


object OnlineOrderMapper {

    fun toKotItems(
        orderProducts: List<OrderProductData>,
        kotBatchId: String = "ONLINE",
        tableNo: String? = null
    ): List<PosKotItemEntity> {
        val now = System.currentTimeMillis()


        Log.d("PRINT", "Mapping ${orderProducts.size} products to KOT items...")

        val kotItems = orderProducts.mapIndexed { index, item ->
            Log.d(
                "PRINT",
                "Item[$index]: name='${item.name}', qty=${item.quantity}, price=${item.priceDouble()}, total=${item.finalTotalDouble()}"
            )
}
        return orderProducts.map { item ->
            PosKotItemEntity(
                id = item.id.ifBlank { "ONLINE-${System.nanoTime()}" },
                categoryName = item.categoryName,
                sessionId = "",
                kotBatchId = kotBatchId,
                tableNo = tableNo ?: "ONLINE",

                productId = item.id, // or another product identifier if you have one
                name = item.name,
                categoryId = item.productCat.ifBlank { "ONLINE" },

                parentId = null,
                isVariant = false,

                basePrice = item.priceDouble(),
                quantity = item.quantity,

                taxRate = (item.taxRate as? Number)?.toDouble() ?: 0.0,
                taxType = "exclusive", // assume online prices are exclusive

                status = "DONE",
                kitchenPrinted = false,

                createdAt = now
            )
        }
    }
}
