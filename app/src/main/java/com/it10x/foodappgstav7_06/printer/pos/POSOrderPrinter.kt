package com.it10x.foodappgstav7_06.printer.pos

import android.util.Log
import com.it10x.foodappgstav7_06.data.PrinterRole
import com.it10x.foodappgstav7_06.data.pos.dao.OrderProductDao
import com.it10x.foodappgstav7_06.data.pos.dao.OrderMasterDao
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import com.it10x.foodappgstav7_06.printer.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class POSOrderPrinter(
    private val printerManager: PrinterManager,
    private val orderMasterDao: OrderMasterDao,
    private val orderProductDao: OrderProductDao
) {

    companion object {
        private const val TAG = "POS_PRINT"
    }

    /**
     * Public API
     * Prints Billing → (delay) → Kitchen
     */
    suspend fun print(orderId: String) = withContext(Dispatchers.IO) {

        val master = orderMasterDao.getByIdSync(orderId)
        val items = orderProductDao.getByOrderIdSync(orderId)

        if (master == null || items.isEmpty()) {
            Log.e(TAG, "❌ Cannot print — order or items missing id=$orderId")
            return@withContext
        }

      //  Log.d(TAG, "🖨 Printing POS Order: $orderId")

        val billing = buildBillingReceipt(master, items)
        val kitchen = buildKitchenReceipt(master, items)

        // ---------- BILLING ----------
        printerManager.printText(
            role = PrinterRole.BILLING,
            text = billing
        ) { success ->
            Log.d(TAG, "Billing print success=$success")
        }

        // small pause so printers don't conflict
        delay(8000)

        // ---------- KITCHEN ----------
        printerManager.printText(
            role = PrinterRole.KITCHEN,
            text = kitchen
        ) { success ->
            Log.d(TAG, "Kitchen print success=$success")
        }
    }


    // =============================
    //   BILLING RECEIPT
    // =============================
    private fun buildBillingReceipt(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): String {

        val alignLeft = "\u001B\u0061\u0000"

        val itemsBlock =
            items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(3)
                val name = item.name.take(16).padEnd(16)
                val price = format(item.basePrice).padStart(6)
                val total = format(item.itemSubtotal).padStart(7)

                "$qty$name$price$total"
            }

        return buildString {
            append(alignLeft)
            append(
                """
------------------------------
PIZZA ITALIA
Bhogpur to Bholath Road
Vill Bhatnura Lubana
M: 99144-74660
------------------------------
Order No : ${order.srno}
Date     : ${formatDate(order.createdAt)}
Type     : ${order.orderType}
------------------------------
QTY ITEM            PRICE TOTAL
------------------------------
$itemsBlock
------------------------------
Item Total      ${format(order.itemTotal)}
GST             ${format(order.taxTotal)}
------------------------------
GRAND TOTAL     ${format(order.grandTotal)}
------------------------------
Thank You!


""".trimIndent()
            )
        }
    }


    // =============================
    //   KITCHEN ORDER SLIP
    // =============================
    private fun buildKitchenReceipt(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): String {

        val alignLeft = "\u001B\u0061\u0000"

        val lines = items.joinToString("\n") {
            "${it.quantity}  ${it.name}"
        }

        return buildString {
            append(alignLeft)
            append(
                """
******** KITCHEN ********
Order No : ${order.srno}
Type     : ${order.orderType}
------------------------
$lines
------------------------


""".trimIndent()
            )
        }
    }


    // =============================
    // HELPERS
    // =============================
    private fun format(value: Double): String =
        "%.2f".format(value)

    private fun formatDate(millis: Long): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale("en", "IN"))
        return sdf.format(Date(millis))
    }
}
