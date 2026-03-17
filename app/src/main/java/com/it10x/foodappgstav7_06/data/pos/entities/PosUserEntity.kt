package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pos_users")
data class PosUserEntity(

    @PrimaryKey
    val userId: String,          // waiter1, waiter2, main, etc.

    val outletId: String,

    val name: String,            // "Waiter 1"
    val role: String,            // MAIN / WAITER / CASHIER

    val pinHash: String,         // 🔥 NEVER store plain password
    val isActive: Boolean = true,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),

    // Sync
    val syncStatus: String = "PENDING",
    val lastSyncedAt: Long? = null
)
