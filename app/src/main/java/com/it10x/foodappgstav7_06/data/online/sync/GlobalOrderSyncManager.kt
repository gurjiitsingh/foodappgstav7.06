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
import com.it10x.foodappgstav7_06.ui.waiterkitchen.WaiterKitchenViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class GlobalOrderSyncManager(
    private val firestore: FirebaseFirestore,
    private val processedDao: ProcessedCloudOrderDao,
    private val kitchenViewModel: KitchenViewModel,
   // private val waiterkitchenViewModel: WaiterKitchenViewModel,
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
                    val source = orderDoc.getString("source") ?: "UNKNOWN"

                    Log.d("KOT_DEBUG", "Processing orderId = $tableNo")

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

                            Log.d("KOT_DEBUG", "In Firestore core Called ${tableNo}")

                            // 🚀 Call ViewModel to process and print KOT
                            kitchenViewModel.saveKotFromFirestoreWaiter(
                                orderType = "DINE_IN",
                                sessionId = sessionId,
                                tableNo = tableNo,
                                cartItems = cartList,
                                deviceId = "WAITER",
                                deviceName = "WAITER",
                                appVersion = "WAITER",
                                role = "FIRESTORE",
                                source = source,
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

        waiterListener?.remove()
        waiterListener = null

        waiterListener = firestore
            .collection("pos_tables")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("SYNC", "❌ Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                for (change in snapshot.documentChanges) {

                    val doc = change.document

                    val tableId = doc.id
                    val source = doc.getString("source") ?: "UNKNOWN"
                    val sessionId = doc.getString("sessionId") ?: tableId
                    val status = doc.getString("status") ?: "UNKNOWN"
                    val active = doc.getBoolean("active") ?: false
                    val updatedAt = doc.getLong("updatedAt") ?: 0L

                    val items = doc.get("items") as? List<Map<String, Any>> ?: emptyList()

                    Log.d("SYNC_FLOW", "🚀 Listener hit → source=$source")

                    // 🔥 LOGGING
                    when (change.type) {
                        DocumentChange.Type.ADDED -> {
                            Log.d("SYNC", "🆕 TABLE ADDED → $tableId")
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("SYNC", "✏️ TABLE UPDATED → $tableId")
                        }
                        DocumentChange.Type.REMOVED -> {
                            Log.d("SYNC", "❌ TABLE REMOVED → $tableId")
                        }
                    }

                    if (items.isEmpty()) {
                        Log.d("SYNC", "🪹 No items in table")
                    } else {
//                        items.forEachIndexed { index, item ->
//                            Log.d(
//                                "SYNC",
//                                "🍽 Item[$index] → ${item["name"]} | Qty: ${item["quantity"]} | Price: ${item["price"]}"
//                            )
//                        }
                    }

                    // ✅ ONLY PROCESS ADDED / MODIFIED
                    if (change.type != DocumentChange.Type.ADDED &&
                        change.type != DocumentChange.Type.MODIFIED
                    ) continue

                    scope.launch(Dispatchers.IO) {
                        try {

                            // 🔐 DEDUP (CORE FIX)
                            val uniqueId = "${tableId}_$updatedAt"

                            val insertResult = processedDao.insert(
                                ProcessedCloudOrderEntity(
                                    orderId = uniqueId,
                                    processedAt = System.currentTimeMillis()
                                )
                            )

                            // 🚫 ALREADY PROCESSED → SKIP
                            if (insertResult == -1L) {
                                Log.d("SYNC", "⏭️ Already processed: $uniqueId")
                                return@launch
                            }

                            // ✅ FIRST TIME ONLY (WAITER or POS both allowed once)
                            Log.d("SYNC", "✅ Processing first time: $uniqueId (source=$source)")

                            kitchenViewModel.replaceKotFromFirestoreWaiterListener(
                                tableId = tableId,
                                sessionId = sessionId,
                                items = items,
                                source = "FIRESTORE"   // ✅ HERE
                            )

                        } catch (e: Exception) {
                            Log.e("SYNC", "❌ Sync failed for table: $tableId", e)
                        }
                    }
                }
            }
    }

    // -------------------- ORDER PROCESSING --------------------


}