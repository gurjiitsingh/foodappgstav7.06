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
    private val kitchenViewModel : KitchenViewModel,
    private val role: PosRole

) {

    private var listener: ListenerRegistration? = null

    // ✅ ADD THIS (missing earlier)
    private val scope = CoroutineScope(Dispatchers.IO)


    fun startListening() {
        Log.d("KOT_DEBUG", "startListening called: role: ${role}")

        // ❌ Waiter should not listen
        if (role == PosRole.WAITER) {
            Log.d("SYNC_DISABLED", "Firestore listener disabled for WAITER device")
            stopListening()
            return
        }
        Log.d("SYNC_DISABLED", "Firestore listener no disabled for MAIN POS device")
        if (listener != null) return

        listener = firestore.collection("waiter_orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null) return@addSnapshotListener
                if (snapshot == null) return@addSnapshotListener

                snapshot.documentChanges.forEach { change ->

                    // Only new documents
                    if (change.type != DocumentChange.Type.ADDED) return@forEach

                    val orderDoc = change.document
                    val orderId = orderDoc.id
                    val tableNo = orderDoc.getString("tableNo") ?: ""
                    val sessionId = orderDoc.getString("sessionId") ?: ""

                    Log.d("SYNC_DEBUG", "Processing orderId = $orderId")

                    scope.launch(Dispatchers.IO) {

                        try {
                            // 🔐 STRONG DB LOCK (atomic style)
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

                            // 🔽 Fetch items
                            val itemsSnapshot = firestore
                                .collection("waiter_orders")
                                .document(orderId)
                                .collection("items")
                                .get()
                                .await()

                            val cartList = itemsSnapshot.documents.map { itemDoc ->

//             Log.d(
//                "ORDER_ITEM_DEBUG",
//                "productId=${itemDoc.id}, categoryId=${itemDoc.getString("categoryId")}, categoryName=${itemDoc.getString("categoryName")}, name=${itemDoc.getString("productName")}"
//            )
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

                            // 🚀 Direct call (NO extra launch inside ViewModel)
                            kitchenViewModel.createKotAndPrintFirestore(
                                orderType = "DINE_IN",
                                sessionId = sessionId,
                                tableNo = tableNo,
                                cartItems = cartList,
                                deviceId = "WAITER",
                                deviceName = "WAITER",
                                appVersion = "WAITER",
                                role ="FIRESTORE"
                            )

                        //    Log.d("SYNC", "Order processed successfully: $orderId")

                        } catch (e: Exception) {
                            Log.e("SYNC", "Error processing order: $orderId", e)
                        }
                    }
                }
            }
    }



    fun stopListening() {
        listener?.remove()
        listener = null
        Log.d("SYNC", "Firestore listener stopped")
    }
}
