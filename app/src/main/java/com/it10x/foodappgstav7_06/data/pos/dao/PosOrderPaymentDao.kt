package com.it10x.foodappgstav7_06.data.pos.dao


import androidx.room.*
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderPaymentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PosOrderPaymentDao {

    // -----------------------------------------------------
    // INSERT
    // -----------------------------------------------------
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: PosOrderPaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(payments: List<PosOrderPaymentEntity>)

    // -----------------------------------------------------
    // ORDER PAYMENTS
    // -----------------------------------------------------
    @Query("""
        SELECT * FROM pos_order_payments
        WHERE orderId = :orderId
        AND isVoided = 0
        ORDER BY createdAt ASC
    """)
    fun getPaymentsForOrder(orderId: String): Flow<List<PosOrderPaymentEntity>>

    @Query("""
        SELECT IFNULL(SUM(amount),0)
        FROM pos_order_payments
        WHERE orderId = :orderId
        AND status = 'SUCCESS'
        AND isVoided = 0
    """)
    suspend fun getTotalPaidForOrder(orderId: String): Double


    // -----------------------------------------------------
    // SETTLEMENT / REPORTS
    // -----------------------------------------------------
    @Query("""
        SELECT IFNULL(SUM(amount),0)
        FROM pos_order_payments
        WHERE mode = :mode
        AND status = 'SUCCESS'
        AND isVoided = 0
        AND createdAt BETWEEN :from AND :to
    """)
    suspend fun getTotalByMode(
        mode: String,
        from: Long,
        to: Long
    ): Double


    // -----------------------------------------------------
    // SYNC
    // -----------------------------------------------------
    @Query("""
        SELECT * FROM pos_order_payments
        WHERE syncStatus != 'SYNCED'
    """)
    suspend fun getPendingSync(): List<PosOrderPaymentEntity>

    @Query("""
        UPDATE pos_order_payments
        SET syncStatus = 'SYNCED',
            lastSyncedAt = :time
        WHERE id = :paymentId
    """)
    suspend fun markSynced(paymentId: String, time: Long)


    // -----------------------------------------------------
    // VOID (REVERSAL)
    // -----------------------------------------------------
    @Query("""
        UPDATE pos_order_payments
        SET isVoided = 1
        WHERE id = :paymentId
    """)
    suspend fun voidPayment(paymentId: String)
}


