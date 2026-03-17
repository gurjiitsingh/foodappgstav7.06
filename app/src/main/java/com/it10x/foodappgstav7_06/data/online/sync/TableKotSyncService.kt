package com.it10x.foodappgstav7_06.data.online.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TableKotSyncService(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {

    suspend fun syncTableSnapshot(
        tableId: String,
       // tableNo: String
    ) {
        try {
            // 1️⃣ Get all KOT items for table
            val kotItems = kotItemDao.getItemsByTable(tableId)

            if (kotItems.isEmpty()) {
                Log.w("TABLE_SYNC", "No KOT items found for table=$tableId")
                return
            }

            val tableRef = firestore
                .collection("table_kot_snapshots")
                .document(tableId)

            val batch = firestore.batch()

            // 2️⃣ Update table metadata
            batch.set(
                tableRef,
                mapOf(
                    "tableNo" to tableId,
                    "updatedAt" to System.currentTimeMillis()
                )
            )

            // 3️⃣ Clear old items (IMPORTANT)
            val oldItems = tableRef
                .collection("items")
                .get()
                .await()

            for (doc in oldItems.documents) {
                batch.delete(doc.reference)
            }

            // 4️⃣ Add latest items (FULL SNAPSHOT)
            kotItems.forEach { item ->

                val itemRef = tableRef
                    .collection("items")
                    .document(item.id ?: UUID.randomUUID().toString())

                batch.set(
                    itemRef,
                    mapOf(
                        "productId" to item.productId,
                        "productName" to item.name,
                        "categoryId" to item.categoryId,
                        "categoryName" to item.categoryName,
                        "quantity" to item.quantity,
                        "note" to item.note,
                        "createdAt" to item.createdAt,
                        "tableNo" to item.tableNo,
                        "sessionId" to item.sessionId
                    )
                )
            }

            // 5️⃣ Commit batch
            batch.commit().await()

            Log.d("TABLE_SYNC", "✅ Snapshot synced for table=$tableId")

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "❌ Failed to sync table snapshot", e)
        }
    }
}