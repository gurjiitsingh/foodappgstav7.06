package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_06.data.pos.entities.PrinterEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrinterDao {

    @Query("SELECT * FROM printers WHERE isActive = 1")
    fun getActivePrinters(): Flow<List<PrinterEntity>>

    @Query("SELECT * FROM printers")
    suspend fun getAll(): List<PrinterEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(printer: PrinterEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(printers: List<PrinterEntity>)

    @Query("DELETE FROM printers")
    suspend fun clear()

}
