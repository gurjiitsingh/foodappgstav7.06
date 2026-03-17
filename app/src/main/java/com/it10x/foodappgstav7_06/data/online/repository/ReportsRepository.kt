package com.it10x.foodappgstav7_06.data.online.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.online.models.CategorySaleData
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import kotlinx.coroutines.tasks.await
import java.util.Date

class ReportsRepository {

    private val db = FirebaseFirestore.getInstance()

    suspend fun getCategorySalesByDate(
        category: String,
        startMillis: Long,
        endMillis: Long
    ): CategorySaleData? {

        val startTimestamp = com.google.firebase.Timestamp(Date(startMillis))
        val endTimestamp = com.google.firebase.Timestamp(Date(endMillis))

        val snapshot = db.collection("orderProducts")
            .whereEqualTo("categoryName", category)
            .whereGreaterThanOrEqualTo("createdAt", startTimestamp)
            .whereLessThanOrEqualTo("createdAt", endTimestamp)
            .get()
            .await()

        val items = snapshot.documents.mapNotNull {
            it.toObject(OrderProductData::class.java)
        }

        val qty = items.sumOf { it.quantity }
        val sales = items.sumOf { it.finalTotalDouble() }

        return CategorySaleData(
            categoryName = category,
            totalQty = qty,
            totalSales = sales
        )
    }
    suspend fun getCategorySales(): List<CategorySaleData> {

        return try {

            val snapshot = db.collection("orderProducts")
                .get()
                .await()

            val items = snapshot.documents.mapNotNull {
                it.toObject(OrderProductData::class.java)
            }

            val grouped = items.groupBy { it.categoryName }

            grouped.map { (category, products) ->

                val qty = products.sumOf { it.quantity }
                val sales = products.sumOf { it.finalTotalDouble() }

                CategorySaleData(
                    categoryName = category,
                    totalQty = qty,
                    totalSales = sales
                )
            }

        } catch (e: Exception) {

            Log.e("REPORTS", "Category sales failed", e)
            emptyList()
        }
    }



}