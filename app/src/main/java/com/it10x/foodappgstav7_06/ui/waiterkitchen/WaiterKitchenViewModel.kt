package com.it10x.foodappgstav7_06.ui.waiterkitchen

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.compose.foundation.lazy.items

import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.pos.repository.CartRepository
import com.it10x.foodappgstav7_06.data.pos.repository.POSOrdersRepository
import com.it10x.foodappgstav7_06.data.pos.usecase.KotToBillUseCase
import com.it10x.foodappgstav7_06.printer.PrinterManager
import com.it10x.foodappgstav7_06.ui.cart.CartViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import com.it10x.foodappgstav7_06.data.pos.repository.KotRepository



import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.online.models.waiter.WaiterOrder
import com.it10x.foodappgstav7_06.data.online.models.waiter.WaiterOrderItem

import com.it10x.foodappgstav7_06.data.pos.repository.WaiterKitchenRepository

import kotlinx.coroutines.tasks.await
class WaiterKitchenViewModel(
    app: Application,
    private val tableId: String,
    private val tableName: String,
    private val sessionId: String,
    private val orderType: String,
    private val repository: POSOrdersRepository,
    private val waiterKitchenRepository: WaiterKitchenRepository,
    private val cartViewModel: CartViewModel,
) : AndroidViewModel(app) {

    private var kotPrintJob: Job? = null
    private val pendingKotItems = mutableListOf<PosKotItemEntity>( )
    private var pendingBatchId: String? = null
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> get() = _loading
    private val kotItemDao =
        AppDatabaseProvider.get(app).kotItemDao()

    private val _sendSuccess = MutableStateFlow(false)
    val sendSuccess: StateFlow<Boolean> = _sendSuccess

    private val kotToBillUseCase =
        KotToBillUseCase(kotItemDao)

    val kotItems: StateFlow<List<PosKotItemEntity>> =
        kotItemDao.getAllKotItems()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    private var isProcessing = false

    private val kotRepository = KotRepository(
        AppDatabaseProvider.get(app).kotBatchDao(),
        AppDatabaseProvider.get(app).kotItemDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val cartRepository = CartRepository(
        AppDatabaseProvider.get(app).cartDao(),
        AppDatabaseProvider.get(app).tableDao()
    )

    private val printerManager =
        PrinterManager(app.applicationContext)

    fun waiterCartTo_FireStore_Bill(
        cartList: List<PosCartEntity>,
        tableNo: String,
        deviceId: String,
        deviceName: String?,
        role : String
    ) {
        if (isProcessing) return   // 🔥 Prevent duplicate presses

        viewModelScope.launch {
            // Always get fresh items from DB
            val dao = AppDatabaseProvider.get(getApplication()).cartDao()
            val latestCart = try {
                dao.getCartItemsByTableId(tableNo).first()
            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Failed to load cart from DB: ${e.message}", e)
                emptyList()
            }

            if (latestCart.isEmpty()) {
                Log.w("WaiterKitchenVM", "⚠️ No items found in DB for table=$tableNo")
                return@launch
            }


            if (isProcessing) return@launch
            isProcessing = true
            _loading.value = true

            try {

                val success = withContext(Dispatchers.IO) {
                    waiterKitchenRepository.sendOrderToFireStore(
                        cartList = latestCart,
                        tableNo = tableNo,
                        sessionId = sessionId,
                        orderType = orderType,
                        deviceId = deviceId,
                        deviceName = deviceName
                    )
                }

                if (!success) {
                    Log.e("WaiterKitchenVM", "❌ Firestore upload failed")
                    return@launch
                }

//                val billSaved = saveCartItemToBillView(
//                    orderType = orderType,
//                    sessionId = sessionId,
//                    tableNo = tableNo,
//                    cartItems = latestCart,
//                    deviceId = deviceId,
//                    deviceName = deviceName,
//                    appVersion = "appVersion",
//                    role = role,
//                )
            //    Log.d("WaiterKitchenVM", " items=${latestCart.size}")
//                if (!billSaved) {
//                    Log.e("WaiterKitchenVM", "❌ Bill save failed")
//                    return@launch
//                }

                repository.clearCart(orderType, tableNo)
                cartRepository.syncCartCount(tableNo)

                Log.d("WaiterKitchenVM", "✅ Firestore + Bill saved successfully")

                // ✅ MOVE HERE
                _sendSuccess.value = true

            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Error in waiterCartToBill", e)
            } finally {

                // ⭐ VERY IMPORTANT
                _loading.value = false
                isProcessing = false
            }
        }





    }




    fun posFireStoreToWaiterBill(
        cartList: List<PosCartEntity>,
        tableNo: String,
        deviceId: String,
        deviceName: String?,
        role : String
    ) {
        if (isProcessing) return   // 🔥 Prevent duplicate presses

        viewModelScope.launch {
            // Always get fresh items from DB
            val dao = AppDatabaseProvider.get(getApplication()).cartDao()
            val latestCart = try {
                dao.getCartItemsByTableId(tableNo).first()
            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Failed to load cart from DB: ${e.message}", e)
                emptyList()
            }

            if (latestCart.isEmpty()) {
                Log.w("WaiterKitchenVM", "⚠️ No items found in DB for table=$tableNo")
                return@launch
            }


            if (isProcessing) return@launch
            isProcessing = true
            _loading.value = true

            try {
                val billSaved = saveCartItemToBillView(
                    orderType = orderType,
                    sessionId = sessionId,
                    tableNo = tableNo,
                    cartItems = cartList,
                    deviceId = deviceId,
                    deviceName = deviceName,
                    appVersion = "appVersion",
                    role = role,
                )

                if (!billSaved) {
                    Log.e("WaiterKitchenVM", "❌ Bill save failed")
                    return@launch
                }

               // repository.clearCart(orderType, tableNo)
                cartRepository.syncCartCount(tableNo)

                Log.d("WaiterKitchenVM", "✅ Firestore + Bill saved successfully")

                // ✅ MOVE HERE
                _sendSuccess.value = true

            } catch (e: Exception) {
                Log.e("WaiterKitchenVM", "❌ Error in waiterCartToBill", e)
            } finally {

                // ⭐ VERY IMPORTANT
                _loading.value = false
                isProcessing = false
            }
        }





    }


    fun replaceKotFromFirestoreWaiterListener(
        tableId: String,
        sessionId: String,
        items: List<Map<String, Any>>,
        source: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {

                // 🚫 SAFETY: Only process Firestore data here
                if (source != "FIRESTORE") {
                    Log.d("SYNC_VM", "⛔ Ignored non-firestore source")
                    return@launch
                }

                // 🔥 STEP 1: DELETE OLD ITEMS
                kotRepository.deleteKotByTable(tableId)

                if (items.isEmpty()) {
                    Log.d("SYNC_VM", "🪹 Table empty after delete: $tableId")
                    return@launch
                }

                // 🔥 STEP 2: MAP ITEMS
                val cartList = items.map { item ->
                    PosCartEntity(
                        sessionId = sessionId,
                        tableId = tableId,
                        productId = item["productId"]?.toString() ?: "",
                        name = item["name"]?.toString() ?: "",
                        categoryId = "",
                        categoryName = item["category"]?.toString() ?: "",
                        parentId = null,
                        isVariant = false,
                        basePrice = (item["price"] as? Number)?.toDouble() ?: 0.0,
                        quantity = (item["quantity"] as? Number)?.toInt() ?: 1,
                        taxRate = 0.0,
                        taxType = "exclusive",
                        note = item["note"]?.toString() ?: "",
                        modifiersJson = "",
                        kitchenPrintReq = false, // 🚫 IMPORTANT (avoid reprint loop)
                        createdAt = System.currentTimeMillis()
                    )
                }

                Log.d("SYNC_VM", "🍽 Syncing ${cartList.size} items from Firestore")

                // 🔥 STEP 3: SAVE LOCALLY (NO FIRESTORE SYNC)
                saveCartItemToBillView(
                    orderType = "DINE_IN",
                    sessionId = sessionId,
                    tableNo = tableId,
                    cartItems = cartList,
                    deviceId = "FIRESTORE_SYNC",
                    deviceName = "FIRESTORE_SYNC",
                    appVersion = "FIRESTORE_SYNC",
                    role = "FIRESTORE_TABLE"
                )

            } catch (e: Exception) {
                Log.e("SYNC_VM", "❌ replaceKotFromFirestore failed", e)
            }
        }
    }

    private suspend fun saveCartItemToBillView(
        orderType: String,
        sessionId: String,
        tableNo: String?,
        cartItems: List<PosCartEntity>,
        deviceId: String,
        deviceName: String?,
        appVersion: String?,
        role: String,
    ): Boolean = withContext(Dispatchers.IO) {

        val tableNo = tableNo?: "";
        try {
            val db = AppDatabaseProvider.get(printerManager.appContext())
            val kotBatchDao = db.kotBatchDao()
            val kotItemDao = db.kotItemDao()

            val batchId = UUID.randomUUID().toString()
            val now = System.currentTimeMillis()

            repository.markAllSent(tableNo)

            val batch = PosKotBatchEntity(
                id = batchId,
                sessionId = sessionId,
                tableNo = tableNo,
                orderType = orderType,
                deviceId = deviceId,
                deviceName = deviceName,
                appVersion = appVersion,
                createdAt = now,
                sentBy = "WAITRER",
                syncStatus = "DONE",
                lastSyncedAt = null
            )

            kotBatchDao.insert(batch)

            Log.d("KOT_DEBUG", "--- WaiterKitchenViewmodel----")

//            cartItems.forEach {
//                Log.d(
//                    "KOT_DEBUG",
//                    "Item: ${it.name} qty=${it.quantity}"
//                )
//            }

            val items = cartItems.map { cart ->
          //    Log.d("KOT_DEBUG", "Saving item: ${cart.name} qty=${cart.quantity}")
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
                    kitchenPrinted = true,
                    status = "DONE",
                    createdAt = now
                )
            }

            kotRepository.insertItemsInBill(tableNo, items,role)


                kotRepository.markDoneAll(tableNo)
                kotRepository.syncKinchenCount(tableNo)
                kotRepository.syncBillCount(tableNo)


            true

        } catch (e: Exception) {
            Log.e("KOT", "❌ Failed to save KOT", e)
            false
        }
    }



    fun getPendingItems(orderRef: String, orderType: String): Flow<List<PosKotItemEntity>> {


        return if (orderType == "DINE_IN") {
            kotItemDao.getPendingItemsForTable(orderRef)
        } else {
            kotItemDao.getPendingItemsForTable(orderType)
          //  kotItemDao.getPendingItemsForSession(orderRef)
        }
    }
     // ✅ POS signal: kitchen completed for table






    fun resetSendSuccess() {
        _sendSuccess.value = false
    }




}





