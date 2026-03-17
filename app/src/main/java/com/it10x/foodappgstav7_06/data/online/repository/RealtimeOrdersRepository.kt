package com.it10x.foodappgstav7_06.data.online.models.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class RealtimeOrdersRepository {

    private val db = FirebaseFirestore.getInstance()
    private var listener: ListenerRegistration? = null

    fun startListening_working(
        onNewOrder: (OrderMasterData) -> Unit
    ) {
        listener = db.collection("orderMaster")
          //  .whereIn("source", listOf("WEB", "APP"))   // ✅ CRITICAL FIX
          //  .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(15)
            .addSnapshotListener { snapshots, error ->

                if (error != null) {
                    Log.e("REALTIME_ORDER", "Listener error", error)
                    return@addSnapshotListener
                }

                if (snapshots == null) return@addSnapshotListener

                for (change in snapshots.documentChanges) {
                    if (change.type == DocumentChange.Type.ADDED) {
                        val order = change.document
                            .toObject(OrderMasterData::class.java)
                            .copy(id = change.document.id)

                        Log.d(
                            "REALTIME_ORDER",
                            "New order detected: ${order.srno}"
                        )

                        onNewOrder(order)
                    }
                }
            }
    }
    fun startListening(
        onNewOrder: (OrderMasterData) -> Unit
    ) {
        // 🔴 COMPLETELY DISABLED DURING MIGRATION
        // Online order listener turned off so POS can run safely
    }
    fun stopListening() {
        listener?.remove()
        listener = null
    }
}
