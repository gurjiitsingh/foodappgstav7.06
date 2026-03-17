package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_06.data.pos.entities.ProcessedCloudOrderEntity

@Dao
interface ProcessedCloudOrderDao {

    @Query("SELECT COUNT(*) FROM processed_cloud_orders WHERE orderId = :id")
    suspend fun isProcessed(id: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: ProcessedCloudOrderEntity): Long
}