package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_devices")
data class PosDeviceEntity(

    @PrimaryKey
    val deviceId: String,          // unique device id

    val outletId: String,

    val deviceName: String,        // "Main Counter", "Waiter Tab 1"
    val role: String,              // MAIN / WAITER

    val assignedUserId: String?,   // waiter1, waiter2 (optional)

    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync
    val syncStatus: String = "PENDING",
    val lastSyncedAt: Long? = null
)
