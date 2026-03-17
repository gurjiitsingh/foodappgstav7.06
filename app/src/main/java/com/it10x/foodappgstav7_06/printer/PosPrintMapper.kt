package com.it10x.foodappgstav7_06.printer

import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderItemEntity
import com.it10x.foodappgstav7_06.data.pos.entities.PosOrderMasterEntity
import java.text.SimpleDateFormat
import java.util.*

object PosPrintMapper {

    fun toPrintOrder(
        order: PosOrderMasterEntity,
        items: List<PosOrderItemEntity>
    ): PrintOrder {
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val printItems = items.map {
            PrintItem(
                name = it.name,
                quantity = it.quantity,
                price = it.finalPricePerItem,
                subtotal = it.finalTotal,
                note = it.note ?: "",
                modifiersJson = it.modifiersJson ?: ""
            )
        }

        return PrintOrder(
            orderNo = order.srno.toString(),
            customerName = "Walk-in", // or order.customerName if you store it
            dateTime = sdf.format(Date(order.createdAt)),
            items = printItems,
            itemTotal = order.itemTotal,
            tax = order.taxTotal,
            discount = order.discountTotal,
            deliveryFee = 0.0,
            grandTotal = order.grandTotal
        )
    }
}
