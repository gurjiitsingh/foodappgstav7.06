package com.it10x.foodappgstav7_06.data.online.models.repository

import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.PrinterType

class PrinterRepository(
    private val prefs: PrinterPreferences
) {

    // -------------------------
    // PRINTER TYPE
    // -------------------------
    fun getPrinterType(role: PrinterRole): PrinterType =
        prefs.getPrinterType(role)

    fun savePrinterType(role: PrinterRole, type: PrinterType) =
        prefs.savePrinterType(role, type)

    // -------------------------
    // BLUETOOTH
    // -------------------------
    fun getBluetoothPrinterAddress(role: PrinterRole): String =
        prefs.getBluetoothPrinterAddress(role)

    fun getBluetoothPrinterName(role: PrinterRole): String =
        prefs.getBluetoothPrinterName(role)

    fun hasBluetoothPrinter(role: PrinterRole): Boolean =
        prefs.getBluetoothPrinterAddress(role).isNotBlank()

    // -------------------------
    // LAN
    // -------------------------
    fun getLanPrinterIP(role: PrinterRole): String =
        prefs.getLanPrinterIP(role)

    fun getLanPrinterPort(role: PrinterRole): Int =
        prefs.getLanPrinterPort(role)

    // -------------------------
    // USB
    // -------------------------
    fun getUSBPrinterName(role: PrinterRole): String =
        prefs.getUSBPrinterName(role)

    fun getUSBPrinterId(role: PrinterRole): Int =
        prefs.getUSBPrinterId(role)
}
