package com.it10x.foodappgstav7_06.data.pos.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.dao.TableDao
import com.it10x.foodappgstav7_06.data.pos.entities.TableEntity
import kotlinx.coroutines.tasks.await

class TableRepository(
    private val dao: TableDao,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val collection = firestore.collection("tables")

    suspend fun syncFromFirestore() {
        try {
            val snapshot = collection.get().await()

            val tables = snapshot.documents.mapNotNull { doc ->
                doc.toObject(TableEntity::class.java)
            }

            dao.clear()
            dao.insertAll(tables)

            Log.d("TABLE_SYNC", "Synced ${tables.size} tables from Firestore")

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "Sync failed", e)
        }
    }

    suspend fun pushToFirestore(table: TableEntity) {
        try {
            collection.document(table.id)
                .set(table)
                .await()

        } catch (e: Exception) {
            Log.e("TABLE_SYNC", "Push failed", e)
        }
    }
}
