package com.it10x.foodappgstav7_06.data.pos.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "printers")
data class PrinterEntity(

    @PrimaryKey
    val printerId: String = "",

    val deviceId: String? = null,

    val outletId: String = "",

    val printerName: String = "",

    // BILLING / KITCHEN / BAR
    val printerType: String = "",

    // LAN / BLUETOOTH / USB
    val connectionType: String = "",

    // -------- NETWORK --------
    val ipAddress: String? = null,
    val port: Int? = null,

    // -------- BLUETOOTH --------
    val macAddress: String? = null,

    // -------- USB --------
    val usbDeviceName: String? = null,

    val printerWidth: Int = 80,

    val isDefault: Boolean = false,

    val isActive: Boolean = true,

    val createdAt: Long = 0L,

    val updatedAt: Long = 0L,

    val syncStatus: String = "PENDING",

    val lastSyncedAt: Long? = null
)


//@Entity(tableName = "printers")
//data class PrinterEntity(
//
//    @PrimaryKey
//    val printerId: String,
//    val deviceId: String? = null,
//    val outletId: String,         // 🔥 VERY IMPORTANT (multi-outlet safe)
//    val printerName: String,
//
//    // BILL / KITCHEN / BAR
//    val printerType: String,
//
//    // LAN / BLUETOOTH / USB
//    val connectionType: String,
//
//    // -------- NETWORK (LAN/WIFI) --------
//    val ipAddress: String? = null,
//    val port: Int? = null,
//
//    // -------- BLUETOOTH --------
//    val macAddress: String? = null,
//
//    // -------- USB --------
//    val usbDeviceName: String? = null,
//
//    val printerWidth: Int = 80,
//    val isDefault: Boolean = false,
//
//    val isActive: Boolean = true,
//
//    val createdAt: Long = System.currentTimeMillis(),
//    val updatedAt: Long = System.currentTimeMillis(),
//    val syncStatus: String = "PENDING",   // PENDING / SYNCED / ERROR
//    val lastSyncedAt: Long? = null,
//
//)
