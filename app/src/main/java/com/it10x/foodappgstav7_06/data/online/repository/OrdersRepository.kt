package com.it10x.foodappgstav7_06.data.online.repository

import android.util.Log
import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.it10x.foodappgstav7_06.data.online.models.CategorySaleData
import kotlinx.coroutines.tasks.await
import java.util.Date
import com.it10x.foodappgstav7_06.utils.createdAtMillis
import java.text.SimpleDateFormat
import java.util.Locale

class OrdersRepository {

    private val db = FirebaseFirestore.getInstance()

    // -----------------------------
    // PAGINATION STATE
    // -----------------------------
    private val pageAnchors = mutableListOf<DocumentSnapshot>()
    private var lastDocument: DocumentSnapshot? = null

    fun resetPagination() {
        pageAnchors.clear()
        lastDocument = null
    }

    // -----------------------------
    // ORDER MASTER
    // -----------------------------
    suspend fun getFirstPage(limit: Long = 10): List<OrderMasterData> {
        return try {
            // 🧠 Try the fast, indexed query first
            val snapshot = db.collection("orderMaster")
                .whereIn("source", listOf("WEB", "APP", "ONLINE"))
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            val docs = snapshot.documents
            if (docs.isNotEmpty()) {
                pageAnchors.clear()
                pageAnchors.add(docs.first())
                lastDocument = docs.last()
            }

            docs.mapNotNull { it.toObject(OrderMasterData::class.java)?.copy(id = it.id) }

        } catch (e: Exception) {
            // ⚠️ Fallback if index is missing or Firestore fails
            android.util.Log.w("ORDER_FETCH", "Indexed query failed, falling back: ${e.message}")

            val snapshot = db.collection("orderMaster")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .limit(limit * 3)
                .get()
                .await()

            val allOrders = snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

            // ✅ Local filter for online/web orders
            val filteredOrders = allOrders.filter {
                it.source?.uppercase() in listOf("WEB", "APP", "ONLINE")
            }.take(limit.toInt())

            filteredOrders
        }
    }


    suspend fun getNextPage(limit: Long = 10): List<OrderMasterData> {
        if (lastDocument == null) return emptyList()

        return try {
            val snapshot = db.collection("orderMaster")
                .whereIn("source", listOf("WEB", "APP", "ONLINE"))
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAfter(lastDocument!!)
                .limit(limit)
                .get()
                .await()

            val docs = snapshot.documents
            if (docs.isNotEmpty()) {
                pageAnchors.add(docs.first())
                lastDocument = docs.last()
            }

            docs.mapNotNull { it.toObject(OrderMasterData::class.java)?.copy(id = it.id) }

        } catch (e: Exception) {
            android.util.Log.w("ORDER_FETCH", "Next page fallback: ${e.message}")

            val snapshot = db.collection("orderMaster")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAfter(lastDocument!!)
                .limit(limit * 3)
                .get()
                .await()

            val allOrders = snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

            val filteredOrders = allOrders.filter {
                it.source?.uppercase() in listOf("WEB", "APP", "ONLINE")
            }.take(limit.toInt())

            filteredOrders
        }
    }


    suspend fun getPrevPage(limit: Long = 10): List<OrderMasterData> {
        if (pageAnchors.size < 2) return emptyList()

        // Move one page back
        pageAnchors.removeLast()
        val prevAnchor = pageAnchors.last()

        return try {
            // 🧠 Try indexed query first
            val snapshot = db.collection("orderMaster")
                .whereIn("source", listOf("WEB", "APP", "ONLINE"))
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAt(prevAnchor)
                .limit(limit)
                .get()
                .await()

            val docs = snapshot.documents
            if (docs.isNotEmpty()) {
                lastDocument = docs.last()
            }

            docs.mapNotNull { it.toObject(OrderMasterData::class.java)?.copy(id = it.id) }

        } catch (e: Exception) {
            // ⚠️ Fallback: no index → fetch more + filter locally
            android.util.Log.w("ORDER_FETCH", "Prev page fallback: ${e.message}")

            val snapshot = db.collection("orderMaster")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAt(prevAnchor)
                .limit(limit * 3)
                .get()
                .await()

            val allOrders = snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

            // ✅ Local filter: only show online/web/app orders
            val filteredOrders = allOrders.filter {
                it.source?.uppercase() in listOf("WEB", "APP", "ONLINE")
            }.take(limit.toInt())

            if (filteredOrders.isNotEmpty()) {
                lastDocument = snapshot.documents.lastOrNull()
            }

            filteredOrders
        }
    }


    // -----------------------------
    // ORDER PRODUCTS (ITEMS) ✅ NEW
    // -----------------------------
    suspend fun getOrderProducts(orderMasterId: String): List<OrderProductData> {

        //  Log.d("ORDER_REPO", "Fetching items for orderId=$orderMasterId")

        val snapshot = db.collection("orderProducts")
            .whereEqualTo("orderMasterId", orderMasterId)
            .get()
            .await()

        return snapshot.documents.mapNotNull {
            it.toObject(OrderProductData::class.java)?.copy(id = it.id)
        }
    }

    suspend fun markOrderAsPrinted(orderId: String) {
        db.collection("orderMaster")
            .document(orderId)
            .update("printed", true)
            .await()
    }



    suspend fun searchOrdersByDate(
        startMillis: Long,
        endMillis: Long,
        limit: Long = 20
    ): List<OrderMasterData> {

        return try {

            val startTimestamp = com.google.firebase.Timestamp(Date(startMillis))
            val endTimestamp = com.google.firebase.Timestamp(Date(endMillis))

            val snapshot = db.collection("orderMaster")
                .whereIn("source", listOf("WEB", "APP", "ONLINE"))
                .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
                .whereLessThanOrEqualTo("createdAt", endTimestamp)
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

        } catch (e: Exception) {

            android.util.Log.e("ORDER_SEARCH", "Date search failed", e)
            emptyList()

        }
    }





    suspend fun getFirstPagePOS(limit: Long = 5): List<OrderMasterData> {

        return try {

            val snapshot = db.collection("orderMaster")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .limit(limit) // fetch more for local filtering
                .get()
                .await()

            val allOrders = snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

            val posOrders = allOrders
                .filter { it.source == "POS" }
                .take(limit.toInt())

            posOrders

        } catch (e: Exception) {

            Log.e("POS_HISTORY", "First page failed", e)
            emptyList()
        }
    }



    suspend fun getNextPagePOS(limit: Long = 10): List<OrderMasterData> {

        if (lastDocument == null) return emptyList()

        return try {

            val snapshot = db.collection("orderMaster")
                .whereEqualTo("source", "POS")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAfter(lastDocument!!)
                .limit(limit)
                .get()
                .await()

            val docs = snapshot.documents

            if (docs.isNotEmpty()) {
                pageAnchors.add(docs.first())
                lastDocument = docs.last()
            }

            docs.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

        } catch (e: Exception) {

            android.util.Log.e("POS_HISTORY", "Next page failed", e)
            emptyList()

        }
    }


    suspend fun getPrevPagePOS(limit: Long = 10): List<OrderMasterData> {

        if (pageAnchors.size < 2) return emptyList()

        pageAnchors.removeLast()
        val prevAnchor = pageAnchors.last()

        return try {

            val snapshot = db.collection("orderMaster")
                .whereEqualTo("source", "POS")
                .orderBy("createdAtMillis", Query.Direction.DESCENDING)
                .startAt(prevAnchor)
                .limit(limit)
                .get()
                .await()

            val docs = snapshot.documents

            if (docs.isNotEmpty()) {
                lastDocument = docs.last()
            }

            docs.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

        } catch (e: Exception) {

            android.util.Log.e("POS_HISTORY", "Prev page failed", e)
            emptyList()

        }
    }




    // -----------------------------
// SIMPLE HISTORY TEST (NO FILTER)
// -----------------------------

    suspend fun searchPOSOrdersByDate(
        startMillis: Long,
        endMillis: Long,
        limit: Long = 100
    ): List<OrderMasterData> {
        Log.d("POS_HISTORY","this is tex")
        return try {

            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val startDate = formatter.format(Date(startMillis))
            val endDate = formatter.format(Date(endMillis))

            Log.d("POS_HISTORY", "StartDate = $startDate")
            Log.d("POS_HISTORY", "EndDate = $endDate")

            val snapshot = db.collection("orderMaster")
                .whereGreaterThanOrEqualTo("orderDate", startDate)
                .whereLessThanOrEqualTo("orderDate", endDate)
                .limit(limit)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull {
                it.toObject(OrderMasterData::class.java)?.copy(id = it.id)
            }

            // Filter POS locally (no index required)
            orders.filter { it.source == "POS" }

        } catch (e: Exception) {

            android.util.Log.e("POS_HISTORY", "POS Date search failed", e)
            emptyList()
        }
    }



    suspend fun getCategorySalesByDate(
        startMillis: Long,
        endMillis: Long
    ): List<CategorySaleData> {

        return try {

            val startTimestamp = com.google.firebase.Timestamp(Date(startMillis))
            val endTimestamp = com.google.firebase.Timestamp(Date(endMillis))

            val snapshot = db.collection("orderProducts")
                .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
                .whereLessThanOrEqualTo("createdAt", endTimestamp)
                .get()
                .await()

            val items = snapshot.documents.mapNotNull {
                it.toObject(OrderProductData::class.java)
            }

            val grouped = items.groupBy { it.categoryName }

            val result = grouped.map { (category, products) ->

                val qty = products.sumOf { it.quantity }

                val sales = products.sumOf { it.finalTotalDouble() }

                CategorySaleData(
                    categoryName = category,
                    totalQty = qty,
                    totalSales = sales
                )
            }

            result.sortedByDescending { it.totalSales }

        } catch (e: Exception) {

            Log.e("CATEGORY_SALES", "Category sales fetch failed", e)
            emptyList()
        }
    }




}
