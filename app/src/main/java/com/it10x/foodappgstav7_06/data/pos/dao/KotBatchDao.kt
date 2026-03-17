package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotBatchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KotBatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(batch: PosKotBatchEntity)

    @Query("""
        SELECT * FROM pos_kot_batch
        WHERE tableNo = :tableNo
        ORDER BY createdAt ASC
    """)
    fun getBatchesForTable(tableNo: String): Flow<List<PosKotBatchEntity>>

    @Query("""
        SELECT * FROM pos_kot_batch
        WHERE id = :batchId
        LIMIT 1
    """)
    suspend fun getById(batchId: String): PosKotBatchEntity?

    @Query("""
        DELETE FROM pos_kot_batch
        WHERE tableNo = :tableNo
    """)
    suspend fun clearForTable(tableNo: String)
}
