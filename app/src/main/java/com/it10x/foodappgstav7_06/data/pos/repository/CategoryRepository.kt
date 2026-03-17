package com.it10x.foodappgstav7_06.data.pos.repository

import com.it10x.foodappgstav7_06.data.pos.dao.CategoryDao
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val dao: CategoryDao
) {

    // Observe all categories
    fun observeCategories(): Flow<List<CategoryEntity>> {
        return dao.getAll()
    }

    // ✅ Create this function (what you are calling)
    suspend fun getCategoryById(id: String): CategoryEntity? {
        return dao.getById(id)
    }

    suspend fun insertAll(list: List<CategoryEntity>) {
        dao.insertAll(list)
    }

    suspend fun clear() {
        dao.clear()
    }

    /**
     * Optional: Keep business logic here (cleaner)
     */
    suspend fun resolveKitchenPrint(product: ProductEntity): Boolean {

        val category = dao.getById(product.categoryId)

        return product.kitchenPrintReq
            ?: category?.kitchenPrintReq
            ?: true
    }
}
