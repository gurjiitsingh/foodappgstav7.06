package com.it10x.foodappgstav7_06.data.online.models.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductSyncRepository(
    private val db: AppDatabase
) {

    private val firestore = FirebaseFirestore.getInstance()

    // --------------------------------------------------
    // ⭐ DOWNLOAD & SAVE CATEGORIES
    // --------------------------------------------------
    suspend fun syncCategories() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("category")
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            CategoryEntity(
                id = doc.id,
                name = data["name"] as? String ?: "",
                desc = data["desc"] as? String ?: "",
                image = data["image"] as? String,
                taxRate = (data["taxRate"] as? Number)?.toDouble(),
                taxType = data["taxType"] as? String,
                outletId = data["outletId"] as? String
            )
        }

      //  Log.d("SYNC_CATEGORIES", "Fetched ${list.size} categories")

        db.categoryDao().clear()
        db.categoryDao().insertAll(list)

     //   Log.d("SYNC_CATEGORIES", "Inserted into Room")
    }

    // --------------------------------------------------
    // ⭐ DOWNLOAD & SAVE PRODUCTS (AS-IS)
    // --------------------------------------------------
    // --------------------------------------------------
// ⭐ DOWNLOAD & SAVE PRODUCTS (AS-IS)
// --------------------------------------------------
    suspend fun syncProducts() = withContext(Dispatchers.IO) {

        val snapshot = firestore
            .collection("products")
            .get()
            .await()

        val list = snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null

            val product = ProductEntity(

                // ------------ CORE ID ------------

                id = doc.id,

                // ------------ DISPLAY ------------

                name = data["name"] as? String
                    ?: error("Product missing name"),

                price = anyToDouble(data["price"]).also {
                    if (it <= 0.0) error("Invalid product price for ${doc.id}")
                },
                discountPrice = (data["discountPrice"] as? Number)?.toDouble(),
                image = data["image"] as? String,

                // ------------ FOOD TYPE ⭐ NEW ------------
                foodType = data["foodType"] as? String,

                // ------------ SORT ORDER ⭐ NEW ------------
                sortOrder = (data["sortOrder"] as? Number)?.toInt() ?: 0,

                // ------------ CATEGORY ------------

                categoryId = data["categoryId"] as? String
                    ?: error("OrderProductData missing categoryId"),

                productCat = data["productCat"] as? String
                    ?: error("OrderProductData missing productCat"),

                // ------------ VARIANTS ------------

                parentId = data["parentId"] as? String,
                baseProductId = data["baseProductId"] as? String,
                hasVariants = data["hasVariants"] as? Boolean ?: false,

                // ------------ STOCK ------------

                stockQty = (data["stockQty"] as? Number)?.toInt() ?: 0,

                // ------------ TAX ------------

                taxRate = (data["taxRate"] as? Number)?.toDouble(),
                taxType = data["taxType"] as? String,

                // ------------ META ------------

                type = data["type"] as? String,   // keep but don't depend on it

                // ------------ POS SEARCH ------------

                searchCode = data["searchCode"] as? String
                    ?: doc.id.takeLast(6),

                // ------------ OUTLET LINK ------------
                outletId = data["outletId"] as? String
            )

            // --------------------------------------------------
            // 🔥 DEBUG LOG — VERIFY DATA FROM FIRESTORE
            // --------------------------------------------------
            Log.d(
                "SYNC_PRODUCT",
                """
name         = ${product.name}
hasVariants  = ${product.hasVariants}
price        = ${product.price}
discount     = ${product.discountPrice ?: "null"}
""".trimIndent()
            )

            product
        }

      //  Log.d("SYNC_PRODUCTS", "Fetched ${list.size} products")

        db.productDao().clear()
        db.productDao().insertAll(list)

      //  Log.d("SYNC_PRODUCTS", "Inserted into Room")
    }

}


private fun anyToDouble(v: Any?): Double =
    when (v) {
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull() ?: 0.0
        else -> 0.0
    }