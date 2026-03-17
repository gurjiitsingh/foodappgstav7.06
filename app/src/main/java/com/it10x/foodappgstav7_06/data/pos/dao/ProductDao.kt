package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.ProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

//    @Query("SELECT * FROM products ORDER BY name")
//    fun getAll(): Flow<List<ProductEntity>>

    @Query("""
    SELECT * FROM products
    ORDER BY sortOrder ASC, name ASC
""")
    fun getAll(): Flow<List<ProductEntity>>


    @Query("""
    SELECT * FROM products
    WHERE 
        (:foodType IS NULL OR foodType = :foodType)
        AND (
            name LIKE '%' || :query || '%'
            OR searchCode LIKE '%' || :query || '%'
        )
    ORDER BY sortOrder ASC, name ASC
""")
    fun searchWithFoodType(
        query: String,
        foodType: String?
    ): Flow<List<ProductEntity>>


    @Query("""
    SELECT * FROM products
    WHERE categoryId = :categoryId
    ORDER BY sortOrder ASC, name ASC
""")
    fun getByCategory(categoryId: String): Flow<List<ProductEntity>>



    @Query("SELECT COUNT(*) FROM products")
    fun getCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<ProductEntity>)

    @Query("DELETE FROM products")
    suspend fun clear()


    @Query("""
    SELECT * FROM products
    WHERE 
        (:foodType IS NULL OR foodType = :foodType)
        AND searchCode = :code
    ORDER BY sortOrder ASC, name ASC
""")
    fun searchExactCodeWithFoodType(
        code: String,
        foodType: String?
    ): Flow<List<ProductEntity>>



}
