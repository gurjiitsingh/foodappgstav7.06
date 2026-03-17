package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name")
    fun getAll(): Flow<List<CategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<CategoryEntity>)

    @Query("DELETE FROM categories")
    suspend fun clear()

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CategoryEntity?



        @Query("SELECT * FROM categories ORDER BY name")
        suspend fun getAllCategories(): List<CategoryEntity>

}




//package com.it10x.foodappgstav7_06.data.pos.dao
//
//import androidx.room.*
//import com.it10x.foodappgstav7_06.data.pos.entities.CategoryEntity
//import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
//import kotlinx.coroutines.flow.Flow
//
//@Dao
//interface CategoryDao {
//
//    @Query("SELECT * FROM categories ORDER BY name")
//    fun getAll(): Flow<List<CategoryEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertAll(list: List<CategoryEntity>)
//
//    @Query("DELETE FROM categories")
//    suspend fun clear()
//}