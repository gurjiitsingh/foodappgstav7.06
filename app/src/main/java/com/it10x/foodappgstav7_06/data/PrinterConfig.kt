package com.it10x.foodappgstav7_06.data

import android.hardware.usb.UsbDevice

data class PrinterConfig(
    val type: PrinterType,
    val bluetoothAddress: String = "",
    val ip: String = "",
    val port: Int = 9100,
    val usbDevice: UsbDevice? = null,
    val role: PrinterRole            // ✅ ADD THIS
)