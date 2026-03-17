package com.it10x.foodappgstav7_06.data

import android.content.Context
import android.hardware.usb.UsbManager
import com.it10x.foodappgstav7_06.data.PrinterConfig
import com.it10x.foodappgstav7_06.printer.PaperSize
import com.it10x.foodappgstav7_06.data.pos.entities.PrinterEntity

class PrinterPreferences(
    private val context: Context
) {

    private val prefs = context.getSharedPreferences("printer_prefs", Context.MODE_PRIVATE)

    private  val KEY_PAPER_SIZE = "paper_size_"
    // -------------------------
    // PRINTER TYPE
    // -------------------------
    fun savePrinterType(role: PrinterRole, type: PrinterType) {
        prefs.edit()
            .putString("printer_${role.name.lowercase()}_type", type.name)
            .apply()
    }

    fun getPrinterType(role: PrinterRole): PrinterType {
        return try {
            PrinterType.valueOf(
                prefs.getString(
                    "printer_${role.name.lowercase()}_type",
                    PrinterType.LAN.name
                )!!
            )
        } catch (e: Exception) {
            PrinterType.LAN
        }
    }

    // -------------------------
    // LAN PRINTER
    // -------------------------
    fun saveLanPrinter(role: PrinterRole, ip: String, port: Int = 9100) {
        prefs.edit()
            .putString("printer_${role.name.lowercase()}_ip", ip)
            .putInt("printer_${role.name.lowercase()}_port", port)
            .apply()
    }

    fun getLanPrinterIP(role: PrinterRole): String =
        prefs.getString("printer_${role.name.lowercase()}_ip", "") ?: ""

    fun getLanPrinterPort(role: PrinterRole): Int =
        prefs.getInt("printer_${role.name.lowercase()}_port", 9100)

    fun setLanPrinterIP(role: PrinterRole, ip: String) {
        prefs.edit().putString("printer_${role.name.lowercase()}_ip", ip).apply()
    }

    fun setLanPrinterPort(role: PrinterRole, port: Int) {
        prefs.edit().putInt("printer_${role.name.lowercase()}_port", port).apply()
    }

    // -------------------------
    // BLUETOOTH PRINTER
    // -------------------------
    fun saveBluetoothPrinter(role: PrinterRole, name: String, address: String) {
        prefs.edit()
            .putString("bt_printer_${role.name.lowercase()}_name", name)
            .putString("bt_printer_${role.name.lowercase()}_address", address)
            .apply()
    }

    fun getBluetoothPrinterName(role: PrinterRole): String =
        prefs.getString("bt_printer_${role.name.lowercase()}_name", "") ?: ""

    fun getBluetoothPrinterAddress(role: PrinterRole): String =
        prefs.getString("bt_printer_${role.name.lowercase()}_address", "") ?: ""

    fun setBluetoothPrinterName(role: PrinterRole, name: String) {
        prefs.edit().putString("bt_printer_${role.name.lowercase()}_name", name).apply()
    }

    fun setBluetoothPrinterAddress(role: PrinterRole, address: String) {
        prefs.edit().putString("bt_printer_${role.name.lowercase()}_address", address).apply()
    }

    // -------------------------
    // USB PRINTER
    // -------------------------
    fun saveUSBPrinter(role: PrinterRole, name: String, deviceId: Int) {
        prefs.edit()
            .putString("usb_printer_${role.name.lowercase()}_name", name)
            .putInt("usb_printer_${role.name.lowercase()}_id", deviceId)
            .apply()
    }

    fun getUSBPrinterName(role: PrinterRole): String =
        prefs.getString("usb_printer_${role.name.lowercase()}_name", "") ?: ""

    fun getUSBPrinterId(role: PrinterRole): Int =
        prefs.getInt("usb_printer_${role.name.lowercase()}_id", -1)

    fun setUSBPrinterName(role: PrinterRole, name: String) {
        prefs.edit().putString("usb_printer_${role.name.lowercase()}_name", name).apply()
    }

    fun setUSBPrinterId(role: PrinterRole, deviceId: Int) {
        prefs.edit().putInt("usb_printer_${role.name.lowercase()}_id", deviceId).apply()
    }

    // -------------------------
    // CLEAR ALL
    // -------------------------
    fun clear() {
        prefs.edit().clear().apply()
    }

// -------------------------
// COMPLETE PRINTER CONFIG (USED BY PRINTING)
// -------------------------
fun getPrinterConfig(role: PrinterRole): PrinterConfig? {

    val type = getPrinterType(role)

    return when (type) {

        PrinterType.BLUETOOTH -> {
            val address = getBluetoothPrinterAddress(role)
            if (address.isBlank()) return null

            PrinterConfig(
                type = PrinterType.BLUETOOTH,
                bluetoothAddress = address,
                role = role
            )
        }

        PrinterType.LAN -> {
            val ip = getLanPrinterIP(role)
            if (ip.isBlank()) return null

            PrinterConfig(
                type = PrinterType.LAN,
                ip = ip,
                port = getLanPrinterPort(role),
                role = role
            )
        }

        PrinterType.USB -> {
            val deviceId = getUSBPrinterId(role)
            if (deviceId == -1) return null

            val usbManager =
                context.getSystemService(Context.USB_SERVICE) as UsbManager

            val device = usbManager.deviceList.values.firstOrNull {
                it.deviceId == deviceId
            } ?: return null

            PrinterConfig(
                type = PrinterType.USB,
                usbDevice = device,
                role = role
            )
        }

        PrinterType.WIFI -> null
    }
}




    fun setPaperSize(role: PrinterRole, size: PaperSize) {
        prefs.edit()
            .putString(KEY_PAPER_SIZE + role.name, size.name)
            .apply()
    }

    fun getPaperSize(role: PrinterRole): PaperSize {
        val value = prefs.getString(KEY_PAPER_SIZE + role.name, PaperSize.MM_58.name)
        return PaperSize.valueOf(value!!)
    }


    fun getPrinterSize(role: PrinterRole): String? {
        return prefs.getString("${role.name}_PRINTER_SIZE", null)
    }

    fun setPrinterSize(role: PrinterRole, size: String) {
        prefs.edit().putString("${role.name}_PRINTER_SIZE", size).apply()
    }

    fun logAllPrinterSettings() {
        PrinterRole.values().forEach { role ->
            val type = getPrinterType(role)
            val size = getPrinterSize(role)
            val usbName = getUSBPrinterName(role)
            val lanIP = getLanPrinterIP(role)
            val lanPort = getLanPrinterPort(role)

            android.util.Log.d(
                "PrinterPrefs",
                "Role: ${role.name}, Type: $type, Size: $size, USB: $usbName, LAN: $lanIP:$lanPort"
            )
        }
    }


    fun buildPrinterEntity(role: PrinterRole): PrinterEntity? {

        val type = getPrinterType(role)

        val printerId = "PRINTER_${role.name}"

        return when (type) {

            PrinterType.BLUETOOTH -> {

                val address = getBluetoothPrinterAddress(role)
                val name = getBluetoothPrinterName(role)

                if (address.isBlank()) return null

                PrinterEntity(
                    printerId = printerId,
                    deviceId = null,
                    outletId = "default_outlet",
                    printerName = name,
                    printerType = role.name,
                    connectionType = "BLUETOOTH",
                    macAddress = address
                )
            }

            PrinterType.LAN -> {

                val ip = getLanPrinterIP(role)

                if (ip.isBlank()) return null

                PrinterEntity(
                    printerId = printerId,
                    deviceId = null,
                    outletId = "default_outlet",
                    printerName = "LAN_${role.name}",
                    printerType = role.name,
                    connectionType = "LAN",
                    ipAddress = ip,
                    port = getLanPrinterPort(role)
                )
            }

            PrinterType.USB -> {

                val name = getUSBPrinterName(role)
                val deviceId = getUSBPrinterId(role)

                if (deviceId == -1) return null

                PrinterEntity(
                    printerId = printerId,
                    deviceId = deviceId.toString(),
                    outletId = "default_outlet",
                    printerName = name,
                    printerType = role.name,
                    connectionType = "USB",
                    usbDeviceName = name
                )
            }

            PrinterType.WIFI -> null
        }
    }



}
