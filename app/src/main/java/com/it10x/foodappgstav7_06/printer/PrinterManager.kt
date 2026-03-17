package com.it10x.foodappgstav7_06.printer

import android.content.Context
import android.util.Log
import com.it10x.foodappgstav7_06.data.PrinterConfig
import com.it10x.foodappgstav7_06.data.PrinterPreferences
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.PrinterType
import com.it10x.foodappgstav7_06.data.print.OutletInfo
import com.it10x.foodappgstav7_06.printer.bluetooth.BluetoothPrinter
import com.it10x.foodappgstav7_06.printer.lan.LanPrinter
import com.it10x.foodappgstav7_06.printer.usb.USBPrinter
import com.it10x.foodappgstav7_06.data.print.OutletMapper
import com.it10x.foodappgstav7_06.data.pos.AppDatabaseProvider
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.ui.sales.SalesUiState
import kotlinx.coroutines.runBlocking

class PrinterManager(
    private val context: Context
) {

    private val prefs by lazy { PrinterPreferences(context) }
    fun appContext(): Context = context.applicationContext
    // --------------------------------
    // TEST PRINT (already OK)
    // --------------------------------
    fun printTest(
        config: PrinterConfig,
        onResult: (Boolean) -> Unit
    ) {
        val roleLabel = config.role.name

        when (config.type) {

            PrinterType.BLUETOOTH -> {
            //    Log.d("PRINT_BT", "Test BT address='${config.bluetoothAddress}'")
                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }
                BluetoothPrinter.printTest(
                    config.bluetoothAddress,
                    roleLabel,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }
                LanPrinter.printTest(
                    config.ip,
                    config.port,
                    roleLabel,
                    onResult
                )
            }



            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    onResult(false)
                    return
                }

                USBPrinter.printTest(
                    context = context,
                    device = device,
                    roleLabel = roleLabel,
                    onResult = onResult
                )
            }






            PrinterType.WIFI -> onResult(false)
        }
    }

    // --------------------------------
    // REAL PRINT (USED BY BUTTON + AUTO)
    // --------------------------------
  fun printText(
    role: PrinterRole,
    text: String,
    onResult: (Boolean) -> Unit = {}
) {
      //  Log.e("PRINT", "printer configured for role=$role")
        Log.e(
            "PRINTTEST",
            "\n================= printText =================\n$text\n=================================================="
        )
    val config = prefs.getPrinterConfig(role)
    if (config == null) {
        Log.e("PRINTTEST", "No printer configured for role=$role")
        onResult(false)
        return
    }

    //Log.d("PRINT", "Printing role=$role type=${config.type}")
    //  var  text1="kljkl"
    when (config.type) {

        PrinterType.BLUETOOTH -> {
            if (config.bluetoothAddress.isBlank()) {
                onResult(false)
                return
            }
            BluetoothPrinter.printText(
                config.bluetoothAddress,
                text,
                onResult
            )
        }

        PrinterType.LAN -> {
            if (config.ip.isBlank()) {
                onResult(false)
                return
            }
            LanPrinter.printText(
                config.ip,
                config.port,
                text,
                onResult
            )
        }

        PrinterType.USB -> {
            val device = config.usbDevice ?: run {
                onResult(false)
                return
            }
            USBPrinter.printText(
                text,
       onResult
            )



        }

        PrinterType.WIFI -> onResult(false)
    }
}


    fun printTextNew(
        role: PrinterRole,
        order: PrintOrder,
        onResult: (Boolean) -> Unit = {}
    ) {
      //  Log.e("PRINT_NEW", "Printing for role=$role")

        // Get printer configuration and preferences
        val config = prefs.getPrinterConfig(role)


        if (config == null) {
            Log.e("PPRINTTEST", "No printer configured for role=$role")
            onResult(false)
            return
        }

        // ✅ Select format based on page size
        val size = prefs.getPrinterSize(role) ?: "80mm"

        // ✅ Auto-load outlet info if not provided

        val outletDao = AppDatabaseProvider.get(context).outletDao()
        val outletEntity = runBlocking { outletDao.getOutlet() }
        val info = OutletMapper.fromEntity(outletEntity)



//        Log.d(
//            "PRINT_NEW",
//            "Outlet Entity = $outletEntity   $info"
//        )

        // ✅ Select format based on printer page size
        val receiptText = when (size) {
            "80mm" -> ReceiptFormatter.billing48(order, info)
            else -> ReceiptFormatter.billing(order, info)
        }



//        Log.d(
//            "PRINT_NEW",
//            "${info.defaultCurrency} Printer type=${config.type}, size=$pageSize, bluetooth=${config.bluetoothAddress}, ip=${config.ip}, "
//        )
        Log.e(
            "PRINTTEST",
            "\n================= BILL NEWTEXT =================\n$receiptText\n=================================================="
        )

        // ✅ Printing logic (kept same as before)
        when (config.type) {
            PrinterType.BLUETOOTH -> {
                if (config.bluetoothAddress.isBlank()) {
                    Log.e("PRINT_NEW", "Bluetooth address missing")
                    onResult(false)
                    return
                }
                BluetoothPrinter.printText(
                    config.bluetoothAddress,
                    receiptText,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    Log.e("PRINT_NEW", "LAN IP missing")
                    onResult(false)
                    return
                }
                LanPrinter.printText(
                    config.ip,
                    config.port,
                    receiptText,
                    onResult
                )
            }

            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    Log.e("PRINT_NEW", "USB device not found")
                    onResult(false)
                    return
                }
                USBPrinter.printText(
                    receiptText,
                    onResult
                )
            }

            PrinterType.WIFI -> {
                Log.e("PRINT_NEW", "WiFi printing not supported yet")
                onResult(false)
            }
        }
    }


    fun printTextKitchen(
        role: PrinterRole,
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        onResult: (Boolean) -> Unit = {}
    ) {
//        Log.d("SYNC_DEBUG", "PRINT CALLED")
//        Log.e("KOT", "STEP 1 → Role=$role")
//        Log.e("KOT", "STEP 2 → Items size=${items.size}")
        val config = prefs.getPrinterConfig(role)
        if (config == null) {
            Log.e("PRINTTEST", "No printer configured for role=$role")
            onResult(false)
            return
        }

        val text = ReceiptFormatter.posKitchen(
            sessionKey ,
            orderType,
            items
        )

        Log.e(
            "PRINTTEST",
            "\n================= KITCHEN RECEIPT =================\n$text\n=================================================="
        )

        //Log.d("PRINT", "Printing role=$role type=${config.type}")
        //  var  text1="kljkl"
        when (config.type) {

            PrinterType.BLUETOOTH -> {
                if (config.bluetoothAddress.isBlank()) {
                    onResult(false)
                    return
                }
                BluetoothPrinter.printText(
                    config.bluetoothAddress,
                    text,
                    onResult
                )
            }

            PrinterType.LAN -> {
                if (config.ip.isBlank()) {
                    onResult(false)
                    return
                }
                LanPrinter.printText(
                    config.ip,
                    config.port,
                    text,
                    onResult
                )
            }

            PrinterType.USB -> {
                val device = config.usbDevice ?: run {
                    onResult(false)
                    return
                }
                USBPrinter.printText(
                    text,
                    onResult
                )


            }

            PrinterType.WIFI -> onResult(false)
        }
    }



    // --------------------------------
    // OPTIONAL
    // --------------------------------
    fun printTestForRole(
        configProvider: () -> PrinterConfig?,
        onResult: (Boolean) -> Unit
    ) {
        val config = configProvider()
        if (config == null) {
            onResult(false)
            return
        }
        printTest(config, onResult)
    }



    fun printSalesReport(
        role: PrinterRole,
        state: SalesUiState,
        onResult: (Boolean) -> Unit = {}
    ) {

        val config = prefs.getPrinterConfig(role)
        if (config == null) {
            onResult(false)
            return
        }

        val size = prefs.getPrinterSize(role) ?: "80mm"

        val outletDao = AppDatabaseProvider.get(context).outletDao()
        val outletEntity = runBlocking { outletDao.getOutlet() }
        val info = OutletMapper.fromEntity(outletEntity)

        val width = if (size == "80mm") 48 else 32

        val text = ReceiptFormatter.salesReport(
            state,
            info,
            width
        )

        printText(role, text, onResult)
    }


    fun printCategorySummary(
        role: PrinterRole,
        category: String,
        totalQty: Int,
        totalAmount: Double,
        onResult: (Boolean) -> Unit = {}
    ) {

        val config = prefs.getPrinterConfig(role)
        if (config == null) {
            onResult(false)
            return
        }

        val size = prefs.getPrinterSize(role) ?: "80mm"
        val width = if (size == "80mm") 48 else 32

        val outletDao = AppDatabaseProvider.get(context).outletDao()
        val outletEntity = runBlocking { outletDao.getOutlet() }
        val info = OutletMapper.fromEntity(outletEntity)

        val text = ReceiptFormatter.salesCategorySummary(
            category,
            totalQty,
            totalAmount,
            info,
            width
        )

        printText(role, text, onResult)
    }


    fun printSingleCategorySales(
        role: PrinterRole,
        category: String,
        items: Map<String, Pair<Int, Double>>,
        onResult: (Boolean) -> Unit = {}
    ) {

        val config = prefs.getPrinterConfig(role)
        if (config == null) {
            onResult(false)
            return
        }

        val size = prefs.getPrinterSize(role) ?: "80mm"
        val width = if (size == "80mm") 48 else 32

        val outletDao = AppDatabaseProvider.get(context).outletDao()
        val outletEntity = runBlocking { outletDao.getOutlet() }
        val info = OutletMapper.fromEntity(outletEntity)

        val text = ReceiptFormatter.salesBySingleCategory(
            category,
            items,
            info,
            width
        )

        printText(role, text, onResult)
    }



}
