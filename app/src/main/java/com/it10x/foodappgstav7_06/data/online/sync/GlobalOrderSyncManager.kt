package com.it10x.foodappgstav7_06.data.online.sync

import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_06.core.PosRole
import com.it10x.foodappgstav7_06.data.pos.KotProcessor
import com.it10x.foodappgstav7_06.data.pos.dao.ProcessedCloudOrderDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.ProcessedCloudOrderEntity
import com.it10x.foodappgstav7_06.ui.kitchen.KitchenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GlobalOrderSyncManager(
    private val firestore: FirebaseFirestore,
    private val processedDao: ProcessedCloudOrderDao,
    private val kitchenViewModel: KitchenViewModel,
    private val role: PosRole
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    private var mainPosListener: ListenerRegistration? = null
    private var waiterListener: ListenerRegistration? = null

    // -------------------- START LISTENERS --------------------

    fun startListening() {
        Log.d("KOT_DEBUG", "startListening called: role=$role")

        stopListening() // always stop first

        when (role) {
            PosRole.MAIN -> startMainPosListener()
            PosRole.WAITER -> startWaiterListener()
        }
    }

    fun stopListening() {
        mainPosListener?.remove()
        mainPosListener = null

        waiterListener?.remove()
        waiterListener = null

        Log.d("SYNC", "All Firestore listeners stopped")
    }

    // -------------------- MAIN POS --------------------

    private fun startMainPosListener() {
        Log.d("KOT_DEBUG", "startMainPosListener called: role=MAIN")

        // Stop previous listener if any
        mainPosListener?.remove()
        mainPosListener = null

        mainPosListener = firestore.collection("waiter_orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->

                    // Only handle new documents
                    if (change.type != DocumentChange.Type.ADDED) return@forEach

                    val orderDoc = change.document
                    val orderId = orderDoc.id
                    val tableNo = orderDoc.getString("tableNo") ?: ""
                    val sessionId = orderDoc.getString("sessionId") ?: ""

                    Log.d("SYNC_DEBUG", "Processing orderId = $orderId")

                    scope.launch(Dispatchers.IO) {
                        try {
                            // 🔐 Strong atomic insert lock
                            val insertResult = processedDao.insert(
                                ProcessedCloudOrderEntity(
                                    orderId = orderId,
                                    processedAt = System.currentTimeMillis()
                                )
                            )

                            if (insertResult == -1L) {
                                Log.d("SYNC", "Already processed (atomic lock): $orderId")
                                return@launch
                            }

                            // Fetch items for this order
                            val orderRef = firestore
                                .collection("waiter_orders")
                                .document(orderId)

                            val itemsSnapshot = orderRef
                                .collection("items")
                                .get()
                                .await()

                            val cartList = itemsSnapshot.documents.map { itemDoc ->
                                PosCartEntity(
                                    sessionId = sessionId,
                                    tableId = tableNo,
                                    productId = itemDoc.getString("productId") ?: "",
                                    name = itemDoc.getString("productName") ?: "",
                                    categoryId = itemDoc.getString("categoryId") ?: "",
                                    categoryName = itemDoc.getString("categoryName") ?: "",
                                    parentId = null,
                                    isVariant = false,
                                    basePrice = itemDoc.getDouble("price") ?: 0.0,
                                    quantity = (itemDoc.getLong("quantity") ?: 1L).toInt(),
                                    taxRate = itemDoc.getDouble("taxRate") ?: 0.0,
                                    taxType = "exclusive",
                                    note = itemDoc.getString("note") ?: "",
                                    modifiersJson = itemDoc.getString("modifiersJson") ?: "",
                                    kitchenPrintReq = itemDoc.getBoolean("kitchenPrintReq") ?: true,
                                    createdAt = System.currentTimeMillis()
                                )
                            }

                            if (cartList.isEmpty()) {
                                Log.w("SYNC", "Cart empty for orderId=$orderId")
                                return@launch
                            }

                            Log.d("KOT_DEBUG", "In Firestore core Called")

                            // 🚀 Call ViewModel to process and print KOT
                            kitchenViewModel.createKotAndPrintFirestore(
                                orderType = "DINE_IN",
                                sessionId = sessionId,
                                tableNo = tableNo,
                                cartItems = cartList,
                                deviceId = "WAITER",
                                deviceName = "WAITER",
                                appVersion = "WAITER",
                                role = "FIRESTORE"
                            )

                            // ✅ Delete order and its items after successful processing
                            try {
                                val batch = firestore.batch()

                                // Delete all items
                                for (itemDoc in itemsSnapshot.documents) {
                                    batch.delete(itemDoc.reference)
                                }

                                // Delete the order document
                                batch.delete(orderRef)

                                batch.commit().await()

                                Log.d(
                                    "SYNC",
                                    "Deleted processed order and its items in batch: $orderId"
                                )

                            } catch (e: Exception) {
                                Log.e(
                                    "SYNC",
                                    "Failed to delete processed order: $orderId",
                                    e
                                )
                            }

                        } catch (e: Exception) {
                            Log.e("SYNC", "Error processing order: $orderId", e)
                        }
                    }
                }
            }
    }

    // -------------------- WAITER --------------------
    // Listen to only MAIN POS orders

    private fun startWaiterListener() {
        Log.d("SYNC", "WAITER listener started")

        waiterListener = firestore
            .collection("main_pos_orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->

                    if (change.type != DocumentChange.Type.ADDED) return@forEach

//                    processOrder(
//                        change.document.id,
//                        "DINE_IN",
//                        "WAITER"
//                    )
                }
            }
    }

    // -------------------- ORDER PROCESSING --------------------

//    private fun processOrder(
//        orderId: String,
//        orderType: String,
//        deviceRole: String
//    ) {
//
//        scope.launch {
//
//            try {
//
////                val insertResult = processedDao.insert(
////                    ProcessedCloudOrderEntity(
////                        orderId,
////                        System.currentTimeMillis()
////                    )
////                )
//
////                if (insertResult == -1L) {
////                    Log.d("SYNC", "Order already processed: $orderId")
////                    return@launch
////                }
//
//                // Fetch items
//                val itemsSnapshot = firestore
//                    .collection(
//                        if (deviceRole == "MAIN")
//                            "waiter_orders"
//                        else
//                            "main_pos_orders"
//                    )
//                    .document(orderId)
//                    .collection("items")
//                    .get()
//                    .await()
//
//                val cartList = itemsSnapshot.documents.map { itemDoc ->
//
//                    PosCartEntity(
//                        sessionId = itemDoc.getString("sessionId") ?: "",
//                        tableId = itemDoc.getString("tableNo") ?: "",
//                        productId = itemDoc.getString("productId") ?: "",
//                        name = itemDoc.getString("productName") ?: "",
//                        categoryId = itemDoc.getString("categoryId") ?: "",
//                        categoryName = itemDoc.getString("categoryName") ?: "",
//                        parentId = null,
//                        isVariant = false,
//                        basePrice = itemDoc.getDouble("price") ?: 0.0,
//                        quantity = (itemDoc.getLong("quantity") ?: 1L).toInt(),
//                        taxRate = itemDoc.getDouble("taxRate") ?: 0.0,
//                        taxType = "exclusive",
//                        note = itemDoc.getString("note") ?: "",
//                        modifiersJson = itemDoc.getString("modifiersJson") ?: "",
//                        kitchenPrintReq = itemDoc.getBoolean("kitchenPrintReq") ?: true,
//                        createdAt = System.currentTimeMillis()
//                    )
//                }
//
//                if (cartList.isEmpty()) {
//                    Log.w("SYNC", "Cart empty for orderId=$orderId")
//                    return@launch
//                }
//
//                Log.d("KOT_DEBUG", "Processing orderId=$orderId for $role")
//
//                // Call kitchenViewModel to handle printing/KOT
//                kitchenViewModel.createKotAndPrintFirestore(
//                    orderType = orderType,
//                    sessionId = cartList.firstOrNull()?.sessionId ?: "",
//                    tableNo = cartList.firstOrNull()?.tableId ?: "",
//                    cartItems = cartList,
//                    deviceId = role.name,
//                    deviceName = role.name,
//                    appVersion = role.name,
//                    role = "FIRESTORE"
//                )
//
//            } catch (e: Exception) {
//
//                Log.e("SYNC", "Error processing order: $orderId", e)
//
//            }
//        }
//    }
}