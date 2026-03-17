package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosCustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosCustomerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: PosCustomerEntity)

    @Update
    suspend fun update(customer: PosCustomerEntity)

    @Query("SELECT * FROM pos_customers WHERE id = :customerId")
    suspend fun getCustomerById(customerId: String): PosCustomerEntity?

    @Query("SELECT * FROM pos_customers WHERE phone = :phone LIMIT 1")
    suspend fun getCustomerByPhone(phone: String): PosCustomerEntity?


    @Query("UPDATE pos_customers SET currentDue = currentDue + :amount WHERE id = :customerId")
    suspend fun increaseDue(customerId: String, amount: Double)

    @Query("UPDATE pos_customers SET currentDue = currentDue - :amount WHERE id = :customerId")
    suspend fun decreaseDue(customerId: String, amount: Double)


    @Query("""
    SELECT * FROM pos_customers
    WHERE phone LIKE '%' || :query || '%'
    OR name LIKE '%' || :query || '%'
    ORDER BY name ASC
""")
    suspend fun searchCustomers(query: String): List<PosCustomerEntity>

//    @Query("""
//    SELECT * FROM pos_customers
//    ORDER BY name ASC
//""")
//    suspend fun getAllCustomers(): List<PosCustomerEntity>


    @Query("SELECT * FROM pos_customers WHERE syncStatus = 'PENDING'")
    suspend fun getPendingSync(): List<PosCustomerEntity>

    @Query("""
    UPDATE pos_customers 
    SET syncStatus = 'SYNCED',
        lastSyncedAt = :time
    WHERE id = :id
""")
    suspend fun markSynced(id: String, time: Long)



    @Query("SELECT * FROM pos_customers ORDER BY currentDue DESC")
    suspend fun getAllCustomers(): List<PosCustomerEntity>


//    @Query("""
//    SELECT * FROM pos_customers
//    WHERE phone LIKE :query || '%'
//    ORDER BY createdAt DESC
//    LIMIT 5
//""")
//    fun searchCustomersByPhone(query: String): Flow<List<PosCustomerEntity>>

//    @Query("SELECT * FROM pos_customers WHERE phone LIKE :query || '%' LIMIT 5")
//    fun searchCustomersByPhone(query: String): Flow<List<PosCustomerEntity>>


    @Query("""
    SELECT * FROM pos_customers 
    WHERE phone LIKE :query || '%' 
    ORDER BY createdAt DESC 
    LIMIT 5
""")
    fun searchCustomersByPhone(query: String): Flow<List<PosCustomerEntity>>



}
