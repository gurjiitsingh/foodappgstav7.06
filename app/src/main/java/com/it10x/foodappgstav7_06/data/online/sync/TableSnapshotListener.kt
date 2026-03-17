package com.it10x.foodappgstav7_06.data.online.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch



class TableSnapshotListener(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {

    private val scope = CoroutineScope(Dispatchers.IO)
    private var listener: ListenerRegistration? = null

    fun startListening(tableId: String) {
        stopListening()

        Log.d("TABLE_SYNC", "👂 Listening for table snapshot: $tableId")

        listener = firestore
            .collection("table_kot_snapshots")
            .document(tableId)
            .collection("items")
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null) {
                    Log.e("TABLE_SYNC", "Listener error", error)
                    return@addSnapshotListener
                }

                scope.launch {
                    try {
                        val newItems = snapshot.documents.map { doc ->

                            PosKotItemEntity(
                                id = doc.id,
                                sessionId = doc.getString("sessionId") ?: "",
                                kotBatchId = "SNAPSHOT",
                                tableNo = doc.getString("tableNo") ?: "",
                                productId = doc.getString("productId") ?: "",
                                name = doc.getString("productName") ?: "",
                                categoryId = doc.getString("categoryId") ?: "",
                                categoryName = doc.getString("categoryName") ?: "",
                                parentId = null,
                                isVariant = false,
                                basePrice = 0.0,
                                quantity = (doc.getLong("quantity") ?: 1L).toInt(),
                                taxRate = 0.0,
                                taxType = "exclusive",
                                note = doc.getString("note") ?: "",
                                modifiersJson = "",
                                kitchenPrintReq = true,
                                kitchenPrinted = true,
                                status = "DONE",
                                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                            )
                        }

                        if (newItems.isEmpty()) {
                            Log.w("TABLE_SYNC", "⚠️ No items in snapshot")
                            return@launch
                        }

                        // 🔥 Replace local table state (IMPORTANT)
                     //   kotItemDao.clearTable(newItems.first().tableNo)
                        kotItemDao.insertAll(newItems)

                        Log.d("TABLE_SYNC", "✅ Table synced from snapshot")

                    } catch (e: Exception) {
                        Log.e("TABLE_SYNC", "❌ Failed to sync snapshot", e)
                    }
                }
            }
    }

    fun stopListening() {
        listener?.remove()
        listener = null
    }
}