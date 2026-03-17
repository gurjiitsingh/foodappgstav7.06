package com.it10x.foodappgstav7_06.data.pos.repository

import androidx.room.withTransaction
import com.it10x.foodappgstav7_06.data.pos.AppDatabase
import com.it10x.foodappgstav7_06.data.pos.entities.OrderSequenceEntity

class OrderSequenceRepository(
    private val db: AppDatabase
) {

    /**
     * 🔐 Atomic, offline-safe order number generator
     * One sequence per outlet per business day
     */
    suspend fun nextOrderNo(
        outletId: String,
        businessDate: String   // yyyyMMdd (LOCAL date)
    ): Int {

        val key = "${outletId}_${businessDate}"
        val now = System.currentTimeMillis()

        return db.withTransaction {

            val dao = db.orderSequenceDao()
            val current = dao.getByKey(key)

            if (current == null) {
                // ✅ First order of the day
                val first = OrderSequenceEntity(
                    key = key,
                    outletId = outletId,
                    businessDate = businessDate,
                    lastOrderNo = 1,
                    updatedAt = now
                )
                dao.insert(first)
                1
            } else {
                // ✅ Increment safely
                val next = current.lastOrderNo + 1
                dao.update(
                    current.copy(
                        lastOrderNo = next,
                        updatedAt = now
                    )
                )
                next
            }
        }
    }
}
