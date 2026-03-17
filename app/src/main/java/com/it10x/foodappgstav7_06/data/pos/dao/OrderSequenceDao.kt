package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.it10x.foodappgstav7_06.data.pos.entities.OrderSequenceEntity

@Dao
interface OrderSequenceDao {

    @Query("""
        SELECT * FROM order_sequence
        WHERE key = :key
        LIMIT 1
    """)
    suspend fun getByKey(key: String): OrderSequenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: OrderSequenceEntity)

    @Update
    suspend fun update(entity: OrderSequenceEntity)
}
