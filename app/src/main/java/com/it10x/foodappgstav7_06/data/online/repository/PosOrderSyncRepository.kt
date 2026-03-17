package com.it10x.foodappgstav7_06.data.online.models.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_06.data.pos.dao.OutletDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar
class PosOrderSyncRepository(
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val outletDao: OutletDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun syncPendingOrders() = withContext(Dispatchers.IO) {

        val outlet = outletDao.getOutlet()
            ?: throw IllegalStateException("Outlet not configured")

        val ownerId = outlet.ownerId
        val outletId = outlet.outletId

        val pendingOrders = orderMasterDao.getPendingSyncOrders()

        if (pendingOrders.isEmpty()) {
            Log.d("ORDER_SYNC", "No pending orders to sync")
            return@withContext
        }

        val batch = firestore.batch()

        pendingOrders.forEach { order ->

            val orderRef = firestore.collection("orderMaster").document(order.id)
            val orderItems = orderProductDao.getByOrderIdSync(order.id)

            val now = order.createdAt
            val createdTimestamp = com.google.firebase.Timestamp(
                now / 1000,
                ((now % 1000) * 1_000_000).toInt()
            )
            val date = Date(now)
            val orderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
            val orderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(date)
            val orderYear = Calendar.getInstance().apply { time = date }.get(Calendar.YEAR)

            // -------- ORDER MASTER --------
            batch.set(orderRef, mapOf(
                "id" to order.id,
                "srno" to order.srno,
                "ownerId" to ownerId,
                "outletId" to outletId,
                "orderType" to order.orderType,
                "tableNo" to order.tableNo,
                "itemTotal" to order.itemTotal,
                "taxTotal" to order.taxTotal,
                "discountTotal" to order.discountTotal,
                "grandTotal" to order.grandTotal,
                "paymentType" to order.paymentMode,
                "paymentStatus" to order.paymentStatus,
                "orderStatus" to order.orderStatus,
                "source" to "POS",
                "createdAt" to createdTimestamp,
                "createdAtMillis" to now,
                "orderDate" to orderDate,
                "orderMonth" to orderMonth,
                "orderYear" to orderYear,
                "syncStatus" to "SYNCED"
            ))

            // -------- ORDER ITEMS --------
            orderItems.forEach { item ->
                val itemRef = firestore.collection("orderProducts").document(item.id)
                val itemDate = Date(item.createdAt)
                val itemOrderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(itemDate)
                val itemOrderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(itemDate)
                batch.set(itemRef, mapOf(
                    "id" to item.id,
                    "orderMasterId" to order.id,
                    "name" to item.name,
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName,
                    "quantity" to item.quantity,
                    "basePrice" to item.basePrice,
                    "itemSubtotal" to item.itemSubtotal,
                    "taxRate" to item.taxRate,
                    "taxType" to item.taxType,
                    "taxAmount" to item.taxAmountPerItem,
                    "taxTotal" to item.taxTotal,
                    "finalPrice" to item.finalPricePerItem,
                    "finalTotal" to item.finalTotal,
                    "createdAt" to com.google.firebase.Timestamp(
                        item.createdAt / 1000,
                        ((item.createdAt % 1000) * 1_000_000).toInt()
                    ),
                    "orderDate" to itemOrderDate,
                    "orderMonth" to itemOrderMonth
                ))
            }


            orderItems.forEach { item ->
                val itemRef = firestore.collection("orderItems").document(item.id)
                val itemDate = Date(item.createdAt)
                val itemOrderDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(itemDate)
                val itemOrderMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(itemDate)
                batch.set(itemRef, mapOf(
                    "id" to item.id,
                    "orderMasterId" to order.id,
                    "name" to item.name,
                    "categoryId" to item.categoryId,
                    "categoryName" to item.categoryName,
                    "quantity" to item.quantity,
                    "basePrice" to item.basePrice,
                    "itemSubtotal" to item.itemSubtotal,
                    "taxRate" to item.taxRate,
                    "taxType" to item.taxType,
                    "taxAmount" to item.taxAmountPerItem,
                    "taxTotal" to item.taxTotal,
                    "finalPrice" to item.finalPricePerItem,
                    "finalTotal" to item.finalTotal,
                    "createdAt" to com.google.firebase.Timestamp(
                        item.createdAt / 1000,
                        ((item.createdAt % 1000) * 1_000_000).toInt()
                    ),
                    "orderDate" to itemOrderDate,
                    "orderMonth" to itemOrderMonth
                ))
            }
        }

        try {
            batch.commit().await()
            Log.d("ORDER_SYNC", "Batch sync success (${pendingOrders.size} orders)")

            orderMasterDao.markOrdersSynced(
                ids = pendingOrders.map { it.id },
                time = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("ORDER_SYNC", "Batch sync failed: ${e.message}", e)
            throw e
        }
    }
}


