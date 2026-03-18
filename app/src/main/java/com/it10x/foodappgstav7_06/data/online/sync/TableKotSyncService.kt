package com.it10x.foodappgstav7_06.data.online.sync

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import kotlinx.coroutines.tasks.await
import okio.Source
import java.util.UUID

class TableKotSyncService(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {

    suspend fun syncTableSnapshot(
        tableId: String,
        source: String,
    ) {
        try {
            val tableRef = firestore
                .collection("pos_tables")
                .document(tableId)

            // 🔥 GET ITEMS FROM ROOM
            val items = kotItemDao.getItemsForTableSync(tableId)
                .filter { it.status == "DONE" }

            // 🔥 CONVERT TO FIRESTORE MAP
            val itemList = items.map {
                mapOf(
                    "productId" to it.productId,
                    "name" to it.name,
                    "quantity" to it.quantity,
                    "price" to it.basePrice,
                    "note" to (it.note ?: ""),
                    "category" to it.categoryName
                )
            }

            val data = mapOf(
                "tableId" to tableId,
                "source" to source,
                "status" to if (itemList.isEmpty()) "EMPTY" else "RUNNING",
                "active" to itemList.isNotEmpty(),
                "items" to itemList,
                "updatedAt" to System.currentTimeMillis()
            )

            tableRef.set(data).await()

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "❌ syncTableSnapshot failed", e)
        }
    }



    suspend fun clearTableSnapshot(tableNo: String) {
        Log.w("TABLE_SYNC", "No KOT items in table=$tableNo")
        try {
            val tableRef = firestore
                .collection("pos_tables")
                .document(tableNo)

            val updateMap = mapOf(
                "status" to "CLOSED",
                "active" to false,
                "items" to emptyList<Map<String, Any>>(),
                "updatedAt" to System.currentTimeMillis()
            )

            tableRef.set(updateMap).await()

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "❌ Failed to clear table snapshot", e)
        }
    }
}