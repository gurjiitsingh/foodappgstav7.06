package com.it10x.foodappgstav7_06.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.entities.TableEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class TableSyncRepository(
    private val db: AppDatabase
) {
    private val firestore = FirebaseFirestore.getInstance()

    suspend fun syncTables() = withContext(Dispatchers.IO) {
        val snapshot = firestore.collection("tables").get().await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            TableEntity(
                id = data["id"] as? String ?: doc.id,
                tableName = data["tableName"] as? String ?: doc.id,
                status = data["status"] as? String ?: "AVAILABLE",
                waiterName = data["waiterName"] as? String,
                waiterId = data["waiterId"] as? String,
                activeOrderId = data["activeOrderId"] as? String,
                guestsCount = (data["guestsCount"] as? Number)?.toInt(),

                // ✅ NEW FIELDS ADDED
                area = data["area"] as? String ?: "General",
                sortOrder = (data["sortOrder"] as? Number)?.toInt(),

                updatedAt = (data["updatedAt"] as? com.google.firebase.Timestamp)
                    ?.toDate()
                    ?.time,
                createdAt = (data["createdAt"] as? com.google.firebase.Timestamp)
                    ?.toDate()
                    ?.time,
                notes = data["notes"] as? String,
                synced = data["synced"] as? Boolean
            )
        }

        Log.d("SYNC_TABLES", "Fetched ${list.size} tables from Firestore")

        db.tableDao().clear()
        db.tableDao().insertAll(list)

        Log.d("SYNC_TABLES", "Inserted ${list.size} tables into Room")
    }
}
