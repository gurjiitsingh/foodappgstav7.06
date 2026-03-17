package com.it10x.foodappgstav7_06.data.pos.dao

import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {


    @Query("""
    SELECT * FROM cart
    WHERE productId = :productId
      AND sessionId = :sessionId
    LIMIT 1
""")
    suspend fun getByIdForSession1(
        productId: String,
        sessionId: String
    ): PosCartEntity?



    @Query("""
    SELECT * FROM cart
    WHERE productId = :productId
      AND tableId = :tableId
    LIMIT 1
""")
    suspend fun getItemByIdForTable(
        productId: String,
        tableId: String
    ): PosCartEntity?





    @Query("SELECT * FROM cart WHERE tableId = :tableId")
    fun getCartItemsByTableId(tableId: String): Flow<List<PosCartEntity>>

    @Query("SELECT * FROM cart WHERE productId = :id AND tableId = :tableId LIMIT 1")
    suspend fun getByIdForTable(id: String, tableId: String): PosCartEntity?


    @Query("DELETE FROM cart WHERE sessionId LIKE :prefix || '%'")
    suspend fun clearCartByPrefix(prefix: String)

    @Query("SELECT * FROM cart WHERE sessionId LIKE :prefix || '%' ORDER BY createdAt ASC")
    fun getCartForSessionPrefix(prefix: String): Flow<List<PosCartEntity>>

    @Query("SELECT * FROM cart WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getCartForSession(sessionId: String): Flow<List<PosCartEntity>>


    @Query("""
    SELECT * FROM cart
    WHERE TableId = :scopeKey
    ORDER BY createdAt ASC
""")
    fun getCartByScope(scopeKey: String): Flow<List<PosCartEntity>>


    @Query("SELECT COUNT(*) FROM cart WHERE tableId = :tableId")
    suspend fun getCartCount(tableId: String): Int



    @Query("""
    SELECT * FROM cart
    WHERE sessionId = :sessionId
    AND sentToKitchen = 0
    ORDER BY createdAt ASC
""")
    fun getUnsentItems(sessionId: String): Flow<List<PosCartEntity>>



    @Query("DELETE FROM cart WHERE sessionId = :sessionId")
    suspend fun clearCart(sessionId: String)

    @Query("DELETE FROM cart WHERE tableId = :tableId")
    suspend fun clearCartByTableId(tableId: String)
    @Query("""
    UPDATE cart
    SET sentToKitchen = 1
    WHERE sessionId = :sessionId
""")
    suspend fun markAllSent(sessionId: String)


    @Query("SELECT * FROM cart WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun getCartForTable(sessionId: String?): Flow<List<PosCartEntity>>

    @Query("SELECT * FROM cart WHERE sessionId = :sessionId")
    fun getCartBySessionId(sessionId: String): Flow<List<PosCartEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(product: PosCartEntity)

    @Query("SELECT * FROM cart")
    suspend fun getCartOnce(): List<PosCartEntity>
    @Update
    suspend fun update(product: PosCartEntity)

    @Query("DELETE FROM cart WHERE productId = :productId AND tableId = :tableNo")
    suspend fun deleteItem(productId: String, tableNo: String)

    @Delete
    suspend fun delete(product: PosCartEntity)



    @Query("SELECT SUM(quantity) FROM cart WHERE tableId = :tableId")
    suspend fun getCartCountForTable(tableId: String): Int?

//
//    @Query("""
//    SELECT * FROM cart
//    WHERE productId = :productId
//    AND tableId = :tableId
//    AND (
//        (note IS NULL AND :note IS NULL)
//        OR note = :note
//    )
//    AND (
//        (modifiersJson IS NULL AND :modifiersJson IS NULL)
//        OR modifiersJson = :modifiersJson
//    )
//    LIMIT 1
//""")
//    suspend fun findMatchingItem(
//        productId: String,
//        tableId: String?,
//        note: String?,
//        modifiersJson: String?
//    ): PosCartEntity?


    @Query("SELECT * FROM cart WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PosCartEntity?

    @Query("DELETE FROM cart WHERE id = :id")
    suspend fun deleteById(id: Long)


    @Query("""
SELECT * FROM cart
WHERE productId = :productId
AND tableId = :tableId
AND note = :note
AND modifiersJson = :modifiersJson
LIMIT 1
""")
    suspend fun findMatchingItem(
        productId: String,
        tableId: String?,
        note: String,
        modifiersJson: String
    ): PosCartEntity?

    @Query("UPDATE cart SET kitchenPrintReq = :value WHERE id = :id")
    suspend fun updatePrintFlag(id: Long, value: Boolean)



}
