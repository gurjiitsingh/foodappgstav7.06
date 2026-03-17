package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.dao.CartDao
import com.it10x.foodappgstav7_06.data.pos.dao.KotBatchDao
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_06.data.pos.dao.TableDao
import com.it10x.foodappgstav7_06.data.pos.dao.VirtualTableDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class POSOrdersRepository(
    private val db: AppDatabase, // 🔹 Keep DB reference for KOT or outlet lookups
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao,
    private val cartDao: CartDao,
    private val tableDao: TableDao,
    private val virtualTableDao: VirtualTableDao
) {

    // -------------------------
    // ORDER DETAILS
    // -------------------------
    suspend fun getOrderById(orderId: String): PosOrderMasterEntity? {
        return orderMasterDao.getOrderById(orderId)
    }


    // -------------------------
    // CART (per table/session)
    // -------------------------


    fun getCartItemsByTableId(tableId: String): Flow<List<PosCartEntity>> {
        return cartDao.getCartItemsByTableId(tableId)
    }
    suspend fun markAllSent(tableId: String) {
        cartDao.markAllSent(tableId)
    }



    // ✅ Clears cart safely depending on order type
    suspend fun clearCart(orderType: String, tableId: String) {
      //  Log.d("CART_DEBUG", " empty cart for table NO.  (${tableId} items)")
        if (!tableId.isNullOrBlank()) {
            cartDao.clearCartByTableId(tableId)      // Table-based session
        }
    }



    // -------------------------
    // TABLE STATE MANAGEMENT
    // -------------------------




    // -------------------------
    // ORDERS
    // -------------------------
    suspend fun getPagedOrders(limit: Int, offset: Int): List<PosOrderMasterEntity> {
        return orderMasterDao.getPagedOrders(limit, offset)
    }


    suspend fun getOrderItems(orderId: String): List<PosOrderItemEntity> {
        return orderProductDao.getByOrderIdSync(orderId)
    }


    // -------------------------
    // BILLING / PAYMENT
    // -------------------------
    suspend fun markOrdersPaid(
        tableNo: String,
        paymentType: String
    ) {
        orderMasterDao.closeTableOrders(
            tableId = tableNo,
            time = System.currentTimeMillis()
        )
    }

    // (Optional) Future: Add KOT management helper functions here if needed
    // e.g. fetch pending KOT items, clear printed KOTs, etc.




//    suspend fun finalizeTableAfterPayment(tableNo: String) {
//
//        // clear KOT
//        db.kotItemDao().clearForTable(tableNo)
//
//        // reset live counters
//        tableDao.updateBill(tableNo, 0, 0.0)
//        tableDao.setKitchenCount(tableNo, 0)
//        tableDao.setCartCount(tableNo, 0)
//
//
//    }

    suspend fun finalizeTableAfterPayment(
        tableNo: String,
        orderType: String
    ) {

        // 🔹 clear KOT always
        db.kotItemDao().clearForTable(tableNo)

        if (orderType == "DINE_IN") {

            // ✅ Reset physical table counters only
            tableDao.updateBill(tableNo, 0, 0.0)
            tableDao.setKitchenCount(tableNo, 0)
            tableDao.setCartCount(tableNo, 0)

        } else {

            // ✅ DELETE virtual table row completely
            virtualTableDao.deleteById(tableNo)
        }
    }


    suspend fun updateGrandTotal(orderId: String, newTotal: Double) {
        orderMasterDao.updateGrandTotal(orderId, newTotal)
    }

    fun getOrdersByDate(start: Long, end: Long): Flow<List<PosOrderMasterEntity>> {
        return orderMasterDao.getOrdersBetween(start, end)
    }

}
