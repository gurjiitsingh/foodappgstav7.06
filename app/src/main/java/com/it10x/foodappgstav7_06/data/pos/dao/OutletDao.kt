package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_06.data.pos.entities.config.OutletEntity

@Dao
interface OutletDao {

    /**
     * Insert or update outlet
     * Since there is ONLY ONE outlet, REPLACE is correct
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveOutlet(outlet: OutletEntity)

    /**
     * Get outlet (single row)
     */
    @Query("SELECT * FROM outlet_config LIMIT 1")
    suspend fun getOutlet(): OutletEntity?

    /**
     * Delete outlet (logout / reset POS)
     */
    @Query("DELETE FROM outlet_config")
    suspend fun deleteOutlet()
}
