package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "processed_cloud_orders",
    indices = [Index(value = ["orderId"], unique = true)]
)
data class ProcessedCloudOrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val orderId: String,
    val processedAt: Long
)