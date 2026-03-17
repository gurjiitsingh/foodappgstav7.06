package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerLedgerEntity

@Dao
interface PosCustomerLedgerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: PosCustomerLedgerEntity)

//    @Query("""
//        SELECT * FROM pos_customer_ledger
//        WHERE customerId = :customerId
//        ORDER BY createdAt ASC
//    """)
//    suspend fun getLedgerForCustomer(customerId: String): List<PosCustomerLedgerEntity>

    @Query("""
        SELECT balanceAfter FROM pos_customer_ledger
        WHERE customerId = :customerId
        ORDER BY createdAt DESC
        LIMIT 1
    """)
    suspend fun getLastBalance(customerId: String): Double?

//    @Query("SELECT * FROM pos_customer_ledger WHERE syncStatus = 'PENDING'")
//    suspend fun getPendingSync(): List<PosCustomerLedgerEntity>

//    @Query("""
//    UPDATE pos_customer_ledger
//    SET syncStatus = 'SYNCED',
//        lastSyncedAt = :time
//    WHERE id = :id
//""")
//    suspend fun markSynced(id: String, time: Long)



    @Query("""
    SELECT * FROM pos_customer_ledger
    WHERE customerId = :customerId
    ORDER BY createdAt DESC
""")
    suspend fun getLedgerForCustomer(customerId: String): List<PosCustomerLedgerEntity>


    @Query("SELECT * FROM pos_customer_ledger WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSync(): List<PosCustomerLedgerEntity>


    @Query("""
    UPDATE pos_customer_ledger 
    SET syncStatus = 'SYNCED', lastSyncedAt = :time 
    WHERE id = :id
""")
    suspend fun markSynced(id: String, time: Long)


}
