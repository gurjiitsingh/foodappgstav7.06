package com.it10x.foodappgstav7_06.data.pos.repository

import com.it10x.foodappgstav7_06.data.pos.dao.CartDao
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.dao.VirtualTableDao

class VirtualTableRepository(
    private val virtualDao: VirtualTableDao,
    private val cartDao: CartDao,
    private val kotDao: KotItemDao
) {

    suspend fun syncCartCount(tableId: String) {
        val count = cartDao.getCartCountForTable(tableId) ?: 0
        virtualDao.setCartCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }

    suspend fun syncBillData(tableId: String) {
        val billCount = kotDao.getBillQtyCount(tableId) ?: 0
        val billAmount = kotDao.sumDoneAmount(tableId) ?: 0.0

        virtualDao.setBillData(
            tableId = tableId,
            count = billCount,
            amount = billAmount,
            time = System.currentTimeMillis()
        )
    }

    suspend fun syncKitchenCount(tableId: String) {
        val count = kotDao.getKitchenCountForTable(tableId) ?: 0
        virtualDao.setKitchenCount(
            tableId = tableId,
            count = count,
            time = System.currentTimeMillis()
        )
    }

    suspend fun removeIfEmpty(tableId: String) {

        val table = virtualDao.getById(tableId) ?: return

        if (table.cartCount == 0 && table.billCount == 0) {
            virtualDao.deleteById(tableId)
//            Log.d("VirtualTableRepo", "Removed empty virtual table: $tableId")
        }
    }
}