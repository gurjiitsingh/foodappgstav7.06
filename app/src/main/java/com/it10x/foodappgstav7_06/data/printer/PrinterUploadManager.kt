package com.it10x.foodappgstav7_06.data.printer

import com.google.firebase.firestore.FirebaseFirestore
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.online.sync.PrinterSyncRepository
import com.it10x.foodappgstav7_06.data.pos.entities.PrinterEntity
import com.it10x.foodappgstav7_06.data.pos.repository.PrinterRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PrinterUploadManager(
    private val prefs: PrinterPreferences,
    private val repository: PrinterRepository
) {

    private val firestore = FirebaseFirestore.getInstance()

    suspend fun uploadPrinter(role: PrinterRole) {

        val repo = PrinterSyncRepository(
            firestore = firestore,
            repository = repository
        )

        val entity = PrinterEntity(

            printerId = role.name,

            outletId = "MAIN",

            printerName =
                prefs.getBluetoothPrinterName(role)
                    .ifBlank { "Printer ${role.name}" },

            printerType = role.name,

            connectionType =
                prefs.getPrinterType(role).name,

            ipAddress =
                prefs.getLanPrinterIP(role),

            port =
                prefs.getLanPrinterPort(role),

            macAddress =
                prefs.getBluetoothPrinterAddress(role),

            usbDeviceName =
                prefs.getUSBPrinterName(role),

            deviceId =
                prefs.getUSBPrinterId(role).toString(),

            printerWidth =
                prefs.getPrinterSize(role)
                    ?.replace("mm", "")
                    ?.toIntOrNull() ?: 80
        )

        withContext(Dispatchers.IO) {
            repo.uploadPrinter(entity)
        }
    }
}
