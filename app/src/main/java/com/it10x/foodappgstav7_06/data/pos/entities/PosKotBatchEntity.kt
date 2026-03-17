package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pos_kot_batch",
    indices = [
        Index(value = ["tableNo"]),
        Index(value = ["createdAt"]),
        Index(value = ["syncStatus"])
    ]
)
data class PosKotBatchEntity(

    @PrimaryKey
    val id: String,                // UUID (one per Send to Kitchen)
    val sessionId: String,
    val tableNo: String?,          // DINE_IN
    val orderType: String,         // DINE_IN | TAKEAWAY | DELIVERY

    val deviceId: String?,
    val deviceName: String?,
    val appVersion: String?,

    val createdAt: Long,           // Send time
    val sentBy: String?,           // cashier / waiter (future)

    // ---- Sync control ----
    val syncStatus: String,        // PENDING | SYNCED | FAILED
    val lastSyncedAt: Long?
)
