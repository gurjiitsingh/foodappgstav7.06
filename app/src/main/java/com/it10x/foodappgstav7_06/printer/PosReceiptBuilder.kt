package com.it10x.foodappgstav7_06.printer

import com.it10x.foodappgstav7_06.data.pos.entities.PosCartEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*

object PosReceiptBuilder {

    fun buildBilling(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): String {

        val alignLeft = "\u001B\u0061\u0000"
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        val itemsBlock = items.joinToString("\n") { item ->
            val qty = item.quantity.toString().padEnd(3)
            val name = item.name.take(16).padEnd(16)
            val price = "%.2f".format(item.finalPricePerItem).padStart(6)
            val total = "%.2f".format(item.finalTotal).padStart(7)
            qty + name + price + total
        }

        return buildString {
            append(alignLeft)
            append(
                """
------------------------------
FOOD APP 2
------------------------------
Order No : ${order.srno}
Type     : ${order.orderType}
Table    : ${order.tableNo ?: "-"}
Payment  : ${order.paymentMode}
Date     : ${sdf.format(Date(order.createdAt))}
------------------------------
QTY ITEM             PRICE TOTAL
--------------------------------
$itemsBlock
--------------------------------
Item Total : ${fmt(order.itemTotal)}
Tax        : ${fmt(order.taxTotal)}
--------------------------------
GRAND TOTAL: ${fmt(order.grandTotal)}
--------------------------------
Thank You!

""".trimIndent()
            )
        }
    }

    private fun fmt(value: Double): String =
        "%.2f".format(value)
}
