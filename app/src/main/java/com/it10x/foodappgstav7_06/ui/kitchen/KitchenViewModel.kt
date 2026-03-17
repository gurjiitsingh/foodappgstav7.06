package com.it10x.foodappgstav7_06.ui.kitchen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.online.sync.TableKotSyncService
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_06.printer.PrintItem
import com.it10x.foodappgstav7_06.printer.PrintOrder
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.printer.ReceiptFormatter
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository
import com.it10x.foodappgstav7_06.data.pos.repository.VirtualTableRepository
import com.it10x.foodappgstav7_06.data.pos.manager.TableSyncManager
class KitchenViewModel(
    app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,

) : AndroidViewModel(app) {


    private val firestore = FirebaseFirestore.getInstance()
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()


    private val kotToBillUseCase =
        KotToBillUseCase(kotItemDao)

    val kotItems: StateFlow<List<PosKotItemEntity>> =
        kotItemDao.getAllKotItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )



    private val kotRepository = KotRepository(
        AppDatabaseProvider.get(app).kotBatchDao(),
        AppDatabaseProvider.get(app).kotItemDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val cartRepository = CartRepository(
        AppDatabaseProvider.get(app).cartDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val virtualTableRepository = VirtualTableRepository(
        AppDatabaseProvider.get(app).virtualTableDao(),
        AppDatabaseProvider.get(app).cartDao(),
        AppDatabaseProvider.get(app).kotItemDao()
    )

    private val tableSyncManager = TableSyncManager(
        tableRepo = kotRepository,
        cartRepo = cartRepository,
        virtualRepo = virtualTableRepository
    )

    private val printerManager =
        PrinterManager(app.applicationContext)



    private val tableKotSyncService = TableKotSyncService(
        firestore,
        kotItemDao
    )


    fun getPendingItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {


        return if (orderType == "DINE_IN" || orderType == "TAKEAWAY" || orderType == "DELIVERY") {
            kotItemDao.getPendingItemsForTable(orderRef)
        } else {
            kotItemDao.getPendingItemsForTable(orderType)
          //  kotItemDao.getPendingItemsForSession(orderRef)
        }
    }
     fun cartToKotMainPOS(
        orderType: String,
        tableNo: String,
        sessionId: String,
        paymentType: String,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    ) {
         Log.d(
             "KOT_DEBUG",
             "cartToKotMainPOS called with orderType=$orderType, tableNo=$tableNo, sessionId=$sessionId"
         )
         //  logAllKotItems()
        viewModelScope.launch {
            _loading.value = true

            // ✅ use sessionId as the real key for cart & KOT
            val sessionKey = sessionId
            val tableId = tableNo!!
            //     Log.d("KITCHEN_DEBUG", "Resolved sessionKey=$sessionKey")

            // ✅ FIX: Use sessionKey (for takeaway & delivery)
            //val cartList = repository.getCartItems(sessionKey, orderType).first()
            val cartList = repository.getCartItemsByTableId(tableId).first()
            //Log.d("KITCHEN_DEBUG", "Cart fetched for type=$orderType, sessionKey=$sessionKey, size=${cartList.size}")

            if (cartList.isEmpty()) {
                Log.w("KITCHEN_DEBUG4", "⚠️ No new items found for orderType=$orderType (sessionKey=$sessionKey)")
                _loading.value = false
                return@launch
            }

            try {
                val now = System.currentTimeMillis()
                val orderId = UUID.randomUUID().toString()

                //  Log.d("KITCHEN_DEBUG4", "Creating new KOT batchId=$orderId for $orderType")

                val kotSaved = saveKotAndPrintKitchen(
                    orderType = orderType,
                    sessionId = sessionId,
                    tableNo = tableNo,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = appVersion,
                    role = role,
                )

                if (!kotSaved) {
                    Log.e("KITCHEN_DEBUG4", " saveKotOnly() failed for session=$sessionKey")
                    return@launch
                }


                repository.clearCart(orderType, tableId)
                tableSyncManager.syncCart(tableId, orderType)
                tableSyncManager.syncBill(tableId, orderType)



            } catch (e: Exception) {
                //  Log.e("KITCHEN_DEBUG", " Exception during placeOrder()", e)
            } finally {
                _loading.value = false
            }
        }
    }



    suspend fun createKotAndPrintFirestore(
        orderType: String,
        sessionId: String,
        tableNo: String,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    ) {
        Log.d("KOT_DEBUG", "Called from: ${Throwable().stackTrace[1]}")

        if (cartItems.isEmpty()) {
            Log.w("KOT_BRIDGE", "⚠️ createKotAndPrint called with empty cartItems")
            return
        }

        _loading.value = true

        try {
            val saved = saveKotAndPrintKitchen(
                orderType = orderType,
                sessionId = sessionId,
                tableNo = tableNo,
                cartItems = cartItems,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                role = role,
            )

            if (!saved) {
                Log.e("KOT_BRIDGE", "❌ Failed to create KOT + Print")
                return
            }

            kotRepository.syncBillCount(tableNo)
        } catch (e: Exception) {
            Log.e("KOT_BRIDGE", "❌ Exception in createKotAndPrint()", e)
        } finally {
            _loading.value = false
        }
    }



    private suspend fun saveKotAndPrintKitchen(
        orderType: String,
        sessionId: String,
        tableNo: String?,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    ): Boolean = withContext(Dispatchers.IO) {
      //  Log.d("KOT", "saveKotAndPrintKitchen Called from: ${Throwable().stackTrace[1]}")
        val tableNo = tableNo?: "";
        try {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val batchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()



            val batch = PosKotBatchEntity(
                id = batchId,
                sessionId = sessionId,
                tableNo = tableNo,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                createdAt = now,
                sentBy = null,
                syncStatus = "DONE",
                lastSyncedAt = null
            )

            kotBatchDao.insert(batch)

            val items = cartItems.map { cart ->
                Log.d("ORDER_TYPE_TRACE",  "orderType=$orderType  session=$sessionId")
                PosKotItemEntity(
                    id = UUID.randomUUID().toString(),
                    sessionId = sessionId,
                    kotBatchId = batchId,
                    tableNo = tableNo,
                    productId = cart.productId,
                    name = cart.name,
                    categoryId = cart.categoryId,
                    categoryName = cart.categoryName,
                    parentId = cart.parentId,
                    isVariant = cart.isVariant,
                    basePrice = cart.basePrice,
                    quantity = cart.quantity,
                    taxRate = cart.taxRate,
                    taxType = cart.taxType,
                    note = cart.note,
                    modifiersJson = cart.modifiersJson,
                    kitchenPrintReq = cart.kitchenPrintReq,
                    kitchenPrinted = false,
                    status = "DONE",
                    createdAt = now
                )
            }



            Log.d("KOT_DEBUG", "---- MainKitchenViewmodel----")
            kotRepository.insertItemsInBill(tableNo, items, role)
            kotRepository.syncBillCount(tableId)


            // 🔥 PRINT (still inside same coroutine)
            // 3️⃣ Print unprinted
            val batchItems = kotItemDao.getItemsByBatchId(batchId)

            if (batchItems.isNotEmpty()) {
               // Log.d("KOT", "Batch is called")
                printerManager.printTextKitchen(
                    PrinterRole.KITCHEN,
                    sessionKey = tableNo,
                    orderType = orderType,
                    items = batchItems
                )

                kotItemDao.markBatchKitchenPrintedBatch(batchId)
              }

          //  logAllKotItemsOnce()


// 🚀 NEW: FIRESTORE TABLE SNAPSHOT SYNC (IMPORTANT)
            try {
                tableKotSyncService.syncTableSnapshot(
                    tableId = tableId,
                 //   tableNo = tableNoSafe
                )

                Log.d("TABLE_SYNC", "Triggered snapshot sync for table=$tableId")

            } catch (e: Exception) {
                Log.e("TABLE_SYNC", "Failed to trigger snapshot sync", e)
            }



            true

        } catch (e: Exception) {
            Log.e("KOT", "❌ Failed to save KOT", e)
            false
        }

    }




    fun logAllKotItems() {
        viewModelScope.launch {
            kotItemDao.getTotalKotItems()
                .collect { items ->
                    Log.d("KITCHEN_DEBUG1", "Total items = ${items.size}")

                    items.forEach { item ->
                        Log.d(
                            "KITCHEN_DEBUG1",
                            "Status=${item.status},print=${item.kitchenPrinted}, Table=${item.tableNo},Name=${item.name},  BatchId=${item.kotBatchId},ID=${item.id}"
                        )
                    }
                }
        }
    }

    fun logAllKotItemsOnce() {
        viewModelScope.launch {

            val items = kotItemDao.getTotalKotItemsOnce()

         //   Log.d("KOT_DEBUG", "Total items = ${items.size}")

//            items.forEach { item ->
//                Log.d(
//                    "KOT_DEBUG",
//                    "Qty=${item.quantity}, " +
//                            "Table=${item.tableNo}, " +
//                            "Name=${item.name}, " +
//                            "Status=${item.status}, " +
//                            "Printed=${item.kitchenPrinted}"
//
//                           // "BatchId=${item.kotBatchId}, " +
//                           // "ID=${item.id}"
//                )
//            }
        }
    }








}





//            val allItems = kotItemDao.getAllItems(tableNo)
//            allItems.forEach {
//                Log.d(
//                    "WAITER_KOT",
//                    "PRINT -> ${it} - ${it.name} | Qty=${it.quantity} | Printed=${it.kitchenPrintReq}"
//                )
//            }


//    fun debugPendingItems(tableNo: String) {
//        viewModelScope.launch {
//            kotItemDao.getPendingItems(tableNo).collect { items ->
//
//                Log.d("KITCHEN_DEBUG", "Pending items count = ${items.size}")
//
//                items.forEach { item ->
//                    Log.d(
//                        "KITCHEN_DEBUG",
//                        "Item -> name=${item.name}, qty=${item.quantity}, status=${item.status}, table=${item.tableNo}"
//                    )
//                }
//            }
//        }
//    }