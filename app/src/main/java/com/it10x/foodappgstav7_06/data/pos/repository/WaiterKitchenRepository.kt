package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.online.models.waiter.WaiterOrder
import com.it10x.foodappgstav7_06.data.online.models.waiter.WaiterOrderItem
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import kotlinx.coroutines.tasks.await


class WaiterKitchenRepository(
    private val firestore: FirebaseFirestore
) {

    suspend fun sendOrderToFireStore(
        cartList: List<PosCartEntity>,
        tableNo: String,
        sessionId: String,
        orderType: String,
        deviceId: String,
        deviceName: String?,

    ): Boolean {

        return try {

            if (cartList.isEmpty()) return false



            val orderId = firestore.collection("waiter_orders").document().id
            val batch = firestore.batch()

            val orderRef = firestore
                .collection("waiter_orders")
                .document(orderId)

            val order = WaiterOrder(
                orderId = orderId,
                tableNo = tableNo,
                sessionId = sessionId,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                status = "PENDING",
                createdAt = System.currentTimeMillis(),
            )

            batch.set(orderRef, order)

            cartList.forEach { cartItem ->

                val itemRef = orderRef
                    .collection("items")
                    .document()

                val orderItem = WaiterOrderItem(
                    productId = cartItem.productId,
                    productName = cartItem.name,
                    categoryId = cartItem.categoryId,
                    categoryName = cartItem.categoryName,
                    quantity = cartItem.quantity,
                    price = cartItem.basePrice,
                    taxRate = cartItem.taxRate,
                    tableNo = tableNo,
                    note = cartItem.note,
                    modifiersJson = cartItem.modifiersJson,
                    sessionId = sessionId,
                    kitchenPrintReq = cartItem.kitchenPrintReq,
                    kitchenPrinted = false
                )

                batch.set(itemRef, orderItem)
            }

            batch.commit().await()

        //    Log.d("WAITER_FIRESTORE", "Order uploaded with ${cartList.size} items")

            true

        } catch (e: Exception) {
            Log.e("WAITER_FIRESTORE", "Upload failed", e)
            false
        }
    }

}

