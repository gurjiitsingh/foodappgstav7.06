package com.it10x.foodappgstav7_06.printer

import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.String

object PrintOrderBuilder {

    // -------------------------
    // BUILD PRINT ORDER
    // -------------------------
    fun build(
        master: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): PrintOrder {

        val printItems = items.map { item ->
            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = item.basePrice,
                subtotal = item.itemSubtotal,
                note = item.note ?: "",
                modifiersJson = item.modifiersJson ?: ""
            )
        }

        return PrintOrder(

            // ---------- CORE ----------
            orderNo = master.srno.toString(),
            dateTime = master.createdAt.formatMillis(),

            // ---------- ORDER TYPE ----------
            orderType = master.orderType,
            tableNo = master.tableNo,



            // ---------- DELIVERY ----------
            customerName = master.customerName?:"Walk-in",
            customerPhone = master.customerPhone,
            dAddressLine1 = master.dAddressLine1,
            dAddressLine2 = master.dAddressLine2,
            dCity = master.dCity,
            dLandmark = master.dLandmark,
            dState = null,
            dZipcode = null,


            // ---------- ITEMS ----------
            items = printItems,

            // ---------- TOTALS ----------
            itemTotal = master.itemTotal,
            deliveryFee = master.deliveryFee,
            tax = master.taxTotal,
            discount = master.discountTotal,
            grandTotal = master.grandTotal
        )
    }

    // -------------------------
    // MERGE MULTIPLE ORDERS (FINAL BILL)
    // -------------------------
    fun mergeOrders(
        orders: List<PosOrderMasterEntity>
    ): PosOrderMasterEntity {

        require(orders.isNotEmpty()) { "Cannot merge empty orders list" }

        val first = orders.first()

        return first.copy(
            id = "FINAL-${first.tableNo}",
            itemTotal = orders.sumOf { it.itemTotal },
            taxTotal = orders.sumOf { it.taxTotal },
            discountTotal = orders.sumOf { it.discountTotal },
            grandTotal = orders.sumOf { it.grandTotal },
            orderStatus = "PAID",
            paymentStatus = "PAID",
            updatedAt = System.currentTimeMillis()
        )
    }

    // -------------------------
    // DATE FORMAT
    // -------------------------
    private fun Long.formatMillis(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        return sdf.format(Date(this))
    }
}
