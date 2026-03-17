package com.it10x.foodappgstav7_06.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.it10x.foodappgstav7_06.data.pos.dao.KotItemDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

class CashierOrderSyncRepository(
    private val firestore: FirebaseFirestore,
    private val kotItemDao: KotItemDao
) {

    private var listener: ListenerRegistration? = null

    fun startListening() {

        Log.d("WAITER_SYNC", "Listening to ALL waiter_orders")

        listener = firestore.collection("waiter_orders")
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    Log.e("WAITER_SYNC", "Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    Log.d("WAITER_SYNC", "Snapshot is NULL")
                    return@addSnapshotListener
                }

                Log.d("WAITER_SYNC", "Total documents = ${snapshot.size()}")

                snapshot.documents.forEach { doc ->

                    Log.d(
                        "WAITER_SYNC",
                        """
                    ------------------------------
                    DocId = ${doc.id}
                    orderId = ${doc.getString("orderId")}
                    tableNo = ${doc.getString("tableNo")}
                    sessionId = ${doc.getString("sessionId")}
                    orderType = ${doc.getString("orderType")}
                    status = ${doc.getString("status")}
                    createdAt = ${doc.getLong("createdAt")}
                    ------------------------------
                    """.trimIndent()
                    )
                }
            }
    }



    fun stopListening() {
        listener?.remove()
        listener = null
    }

    fun debugPrintAllKotItems() {

        CoroutineScope(Dispatchers.IO).launch {

            kotItemDao.getAllKotItems().collect { items ->

                Log.d("WAITER_SYNC", "Total KOT items in DB = ${items.size}")

                items.forEach { item ->

                    Log.d(
                        "WAITER_SYNC",
                        "id=${item.id}, table=${item.tableNo}, " +
                                "session=${item.sessionId}, product=${item.name}, " +
                                "qty=${item.quantity}, price=${item.basePrice}, " +
                                "status=${item.status}, batch=${item.kotBatchId}"
                    )
                }
            }
        }
    }

}
