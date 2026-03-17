package com.it10x.foodappgstav7_06.data.pos.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.printer.PrintOrderBuilder
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.mapper.OnlineOrderMapper
import com.it10x.foodappgstav7_06.data.mapper.PosOrderToKotMapper
import com.it10x.foodappgstav7_06.printer.ReceiptFormatter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger

import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.print.OutletMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
// 🔹 NEW (for atomic order no + API 24 safe date)
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class POSOrdersViewModel(
    private val repository: POSOrdersRepository,
    private val printerManager: PrinterManager
) : ViewModel() {



    val loading: StateFlow<Boolean> get() = _loading
    private val _loading = MutableStateFlow(false)

    val pageIndex = MutableStateFlow(0)
    private val limit = 10
    private val srNoCounter = AtomicInteger(1)


    private val _orders = MutableStateFlow<List<PosOrderMasterEntity>>(emptyList())
    val orders: StateFlow<List<PosOrderMasterEntity>> = _orders

    fun searchOrdersByDate(dateMillis: Long) {

        val startOfDay = dateMillis
        val endOfDay = startOfDay + 86400000

        viewModelScope.launch {

            repository.getOrdersByDate(startOfDay, endOfDay)
                .collect { list ->
                    _orders.value = list
                }
        }
    }

    // 🔹 NEW: API-24 safe business date (yyyyMMdd)
    private fun businessDate(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            .format(Date())
    }
    // -------------------------
    // PAGINATION
    // -------------------------
    fun loadFirstPage() = loadOrders(0)
    fun loadNextPage() = loadOrders(pageIndex.value + 1)
    fun loadPrevPage() {
        val prev = if (pageIndex.value > 0) pageIndex.value - 1 else 0
        loadOrders(prev)
    }

    private fun loadOrders(page: Int) {
        viewModelScope.launch {
            _loading.value = true
            pageIndex.value = page
            val offset = page * limit
            val pagedOrders = repository.getPagedOrders(limit, offset)
//            pagedOrders.forEach {
//                Log.d("ORDER_SRNO", "Loaded order id=${it.id} srno=${it.srno}")
//            }

            _orders.value = pagedOrders.sortedByDescending { it.createdAt }
            _loading.value = false
        }
    }




    // -------------------------
    // PRINT ORDERS (AUTO + MANUAL + BUTTON)
    // -------------------------
    private fun printOrderStandard(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>,
        role: String
    ) {
        Log.d("PRINT_SOURCE", "🟢 POSOrdersViewModel.printOrderStandard CALLED")

        viewModelScope.launch {

          //  Log.d("OUTLET_PRINT", "📨 Building PrintOrder…")

            val printOrder = PrintOrderBuilder.build(order, items)

            // ---------------- OUTLET FROM ROOM ----------------
            val db = AppDatabaseProvider.get(printerManager.appContext())
        //    Log.d("OUTLET_DB_PRINT", "DB Path Print = ${db.openHelper.readableDatabase.path}")

        //    Log.d("OUTLET_PRINT", "🔍 Fetching outlet from Room…")

            val outlet = withContext(Dispatchers.IO) {
                db.outletDao().getOutlet()
            }

            if (outlet == null) {
                Log.e("OUTLET_PRINT", "❌ Outlet is NULL — using default title")
            } else {
              //  Log.d("OUTLET_PRINT", "✅ Outlet Loaded")
             //   Log.d("OUTLET_PRINT", "name=${outlet.outletName}")
             //   Log.d("OUTLET_PRINT", "city=${outlet.city}")
             //   Log.d("OUTLET_PRINT", "phone=${outlet.phone}")
            }



          // ---------------- BILLING PRINT ----------------


if(role == "bill") {
    printerManager.printTextNew(PrinterRole.BILLING, printOrder)
}
            // SMALL DELAY
            kotlinx.coroutines.delay(150)


            //KITCHEN PRINT ONLINE ORDER WHEN BUTTON PRESSED
            if(role == "kitchen") {
                val kotItems = PosOrderToKotMapper.toKotItems(items )

                printerManager.printTextKitchen(
                    PrinterRole.KITCHEN,
                    sessionKey = order.srno.toString(),
                    orderType = order.orderType,
                    items = kotItems
                )
            }

        }
    }


    // -------------------------
    // ORDER DETAILS
    // -------------------------
    fun getOrderProducts(orderId: String): StateFlow<List<PosOrderItemEntity>> {
        val flow = MutableStateFlow<List<PosOrderItemEntity>>(emptyList())
        viewModelScope.launch {
            flow.value = repository.getOrderItems(orderId)
        }
        return flow
    }

    // -------------------------
    // MANUAL PRINT OLD ORDER
    // -------------------------
    fun printOrder(orderId: String,role: String) {
        viewModelScope.launch {
            _loading.value = true
            try {

              //  Log.d("POS_PRINT", "Print requested for orderId=$orderId")

                val order = repository.getOrderById(orderId)
                if (order == null) {
                    Log.e("POS_PRINT", "Order NOT FOUND for orderId=$orderId")
                    return@launch
                }

                val items = repository.getOrderItems(orderId)
                if (items.isEmpty()) {
                    Log.d(
                        "ORDER_SRNO",
                        "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
                    )
                    return@launch
                }

//                Log.d(
//                    "ORDER_SRNO",
//                    "Printing orderId=$orderId srno=${order.srno} items=${items.size}"
//                )

    printOrderStandard(order, items, role)


            } catch (e: Exception) {
                Log.e("POS_PRINT", "Exception while printing order", e)
            } finally {
                _loading.value = false
            }
        }



    }








//    private suspend fun saveKotOnly(
//        orderType: String,
//        tableNo: String?,
//        cartItems: List<PosCartEntity>,
//        deviceId: String,
//        deviceName: String?,
//        appVersion: String?
//    ): Boolean {
//        return try {
//            val db = AppDatabaseProvider.get(printerManager.appContext())
//            val kotBatchDao = db.kotBatchDao()
//            val kotItemDao = db.kotItemDao()
//
//            val batchId = UUID.randomUUID().toString()
//            val now = System.currentTimeMillis()
//            repository.markAllSent(tableNo ?: orderType)
//          //  Log.d("KOT_STEP", "Marked ${items.size} items as sent to kitchen")
//            val batch = PosKotBatchEntity(
//                id = batchId,
//                tableNo = tableNo ?: orderType,
//                orderType = orderType,
//                deviceId = deviceId,
//                deviceName = deviceName,
//                appVersion = appVersion,
//                createdAt = now,
//                sentBy = null,
//                syncStatus = "PENDING",
//                lastSyncedAt = null
//            )
//
//            withContext(Dispatchers.IO) {
//                kotBatchDao.insert(batch)
//                Log.d("KOT_DEBUG", "Saved ${cartItems.size} KOT items for tableNo=${tableNo ?: orderType}")
//                val items = cartItems.map { cart ->
//                    PosKotItemEntity(
//                        id = UUID.randomUUID().toString(),
//                        kotBatchId = batchId,
//                        tableNo = tableNo ?: orderType,
//                        productId = cart.productId,
//                        name = cart.name,
//                        categoryId = cart.categoryId,
//                        parentId = cart.parentId,
//                        isVariant = cart.isVariant,
//                        basePrice = cart.basePrice,
//                        quantity = cart.quantity,
//                        taxRate = cart.taxRate,
//                        taxType = cart.taxType,
//                        print = false,
//                        status = "PENDING",   // ✅ REQUIRED
//                        createdAt = now
//                    )
//                }
//
//                kotItemDao.insertAll(items)
//            }
//
//            Log.d("KOT", "✅ KOT SAVED: batch=$batchId items=${cartItems.size}")
//            true
//
//        } catch (e: Exception) {
//            Log.e("KOT", "❌ Failed to save KOT", e)
//            false
//        }
//    }

//    fun debugReadKot(tableNo: String) {
//        viewModelScope.launch {
//            val db = AppDatabaseProvider.get(printerManager.appContext())
//            val batches = db.kotBatchDao().getBatchesForTable(tableNo).first()
//            val items = db.kotItemDao().getItemsForTable(tableNo).first()
//
//            Log.d("KOT_READ", "Batches=${batches.size}")
//            Log.d("KOT_READ", "Items=${items.size}")
//
//            items.forEach {
//                Log.d("KOT_ITEM", "${it.name} x${it.quantity}")
//            }
//        }
//    }

}
