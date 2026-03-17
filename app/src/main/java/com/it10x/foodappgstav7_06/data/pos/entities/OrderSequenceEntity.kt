package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 🔑 Controls POS order numbering (offline-safe)
 * One row per (outletId + businessDate)
 */
@Entity(tableName = "order_sequence")
data class OrderSequenceEntity(

    @PrimaryKey
    val key: String,
    // Format: {outletId}_{yyyyMMdd}

    val outletId: String,

    val businessDate: String,
    // yyyyMMdd (LOCAL date, not UTC)

    val lastOrderNo: Int,
    // Last issued running number for that day

    val updatedAt: Long
)
