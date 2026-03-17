package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.VirtualTableEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VirtualTableDao {

    @Query("SELECT * FROM virtual_tables WHERE orderType = :type ORDER BY createdAt ASC")
    fun observeByType(type: String): Flow<List<VirtualTableEntity>>

    @Query("SELECT * FROM virtual_tables WHERE orderType = :type ORDER BY createdAt ASC")
    suspend fun getByType(type: String): List<VirtualTableEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(table: VirtualTableEntity)

    @Update
    suspend fun update(table: VirtualTableEntity)

    @Query("DELETE FROM virtual_tables WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM virtual_tables WHERE id = :tableId LIMIT 1")
    suspend fun getById(tableId: String): VirtualTableEntity?



    @Query("UPDATE virtual_tables SET cartCount = :count, updatedAt = :time WHERE id = :tableId")
    suspend fun setCartCount(tableId: String, count: Int, time: Long)

    @Query("UPDATE virtual_tables SET billCount = :count, billAmount = :amount, updatedAt = :time WHERE id = :tableId")
    suspend fun setBillData(tableId: String, count: Int, amount: Double, time: Long)



    @Query("UPDATE virtual_tables SET kitchenCount = :count, updatedAt = :time WHERE id = :tableId")
    suspend fun setKitchenCount(tableId: String, count: Int, time: Long)

    @Query("""
SELECT SUM(quantity) 
FROM pos_kot_items 
WHERE tableNo = :tableNo
""")
    suspend fun getBillQtyCount(tableNo: String): Int?


}