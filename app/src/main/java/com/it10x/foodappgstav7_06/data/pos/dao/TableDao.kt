package com.it10x.foodappgstav7_06.data.pos.dao

import android.util.Log
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.it10x.foodappgstav7_06.data.pos.entities.TableEntity
import kotlinx.coroutines.flow.Flow


@Dao
interface TableDao {



    @Query("SELECT * FROM tables WHERE tableName = :tableName LIMIT 1")
    suspend fun getTable(tableName: String): TableEntity?
    @Query("SELECT * FROM tables WHERE id = :tableId LIMIT 1")
    suspend fun getById(tableId: String): TableEntity?
    @Query("SELECT * FROM tables ORDER BY id ASC")
    suspend fun getAll(): List<TableEntity>

    @Query("DELETE FROM tables")
    suspend fun clear()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<TableEntity>)



//    @Query("UPDATE tables SET status = :status WHERE id = :tableId")
//    suspend fun updateStatus1(tableId: String, status: String)

//    suspend fun updateStatus(tableId: String, status: String) {
//        Log.d(
//            "TABLE_TEST",
//            "updateStatus called → tableId=$tableId status=$status"
//        )
//        updateStatus1(tableId, status)
//    }



    @Query("UPDATE tables SET status = :status WHERE tableName = :tableName")
    suspend fun updateStatusByName(tableName: String, status: String)

    @Query("SELECT * FROM tables ORDER BY id ASC")
    suspend fun getAllTables(): List<TableEntity>
    // ✅ when order starts
    @Query("""
        UPDATE tables
        SET activeOrderId = :orderId,
            status = 'OCCUPIED'
        WHERE id = :tableId
    """)
    suspend fun setActiveOrder(tableId: String, orderId: String)

    // ✅ when table is closed
    @Query("""
        UPDATE tables
        SET activeOrderId = NULL,
            status = 'AVAILABLE'
        WHERE id = :tableId
    """)
    suspend fun clearActiveOrder(tableId: String)


    @Query("UPDATE tables SET status = 'AVAILABLE' WHERE tableName = :tableName")
    suspend fun closeTable(tableName: String)


    @Query("UPDATE tables SET cartCount = :count WHERE id = :tableId")
    suspend fun setCartCount(tableId: String, count: Int)

    @Query("""
UPDATE tables 
SET kitchenCount = :count 
WHERE id = :tableId
""")
    suspend fun setKitchenCount(tableId: String, count: Int)


    @Query("SELECT * FROM tables ORDER BY sortOrder")
    fun observeAllTables(): Flow<List<TableEntity>>



    @Query("""
UPDATE tables
SET billCount = :count,
    billAmount = :amount
WHERE id = :tableNo
""")
    suspend fun updateBill(tableNo: String, count: Int, amount: Double)


    @Query("""
SELECT * FROM tables 
WHERE area = :area
ORDER BY sortOrder
""")
    fun observeTablesByArea(area: String): Flow<List<TableEntity>>


}

