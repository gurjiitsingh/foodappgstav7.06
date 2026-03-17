package com.it10x.foodappgstav7_06.data.pos.manager

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_06.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_06.data.pos.repository.VirtualTableRepository

object OrderType {
    const val DINE_IN = "DINE_IN"
    const val TAKEAWAY = "TAKEAWAY"
    const val DELIVERY = "DELIVERY"
}

class TableSyncManager(
    private val tableRepo: KotRepository,
    private val cartRepo: CartRepository,
    private val virtualRepo: VirtualTableRepository
) {

    suspend fun syncCart(tableId: String, orderType: String) {

        Log.d("TableSyncManager", "syncCart -> tableId: $tableId, type: $orderType")

        when (orderType) {

            OrderType.DINE_IN -> {
                cartRepo.syncCartCount(tableId)
            }

            OrderType.TAKEAWAY,
            OrderType.DELIVERY -> {
                virtualRepo.syncCartCount(tableId)
            }

            else -> {
                Log.e("TableSyncManager", "Unknown orderType: $orderType")
            }
        }
    }

    suspend fun syncBill(tableId: String, orderType: String) {

        when (orderType) {

            OrderType.DINE_IN -> {
                tableRepo.syncBillCount(tableId)
            }

            OrderType.TAKEAWAY,
            OrderType.DELIVERY -> {

                // 1️⃣ Update bill data
                virtualRepo.syncBillData(tableId)

                // 2️⃣ 🔥 Remove row if empty
                virtualRepo.removeIfEmpty(tableId)
            }

            else -> {
                Log.e("TableSyncManager", "Unknown orderType: $orderType")
            }
        }
    }

    suspend fun syncKitchen(tableId: String, orderType: String) {

        when (orderType) {

            OrderType.DINE_IN -> {
                tableRepo.syncKinchenCount(tableId)
            }

            OrderType.TAKEAWAY,
            OrderType.DELIVERY -> {
                virtualRepo.syncKitchenCount(tableId)
            }

            else -> {
                Log.e("TableSyncManager", "Unknown orderType: $orderType")
            }
        }
    }
}