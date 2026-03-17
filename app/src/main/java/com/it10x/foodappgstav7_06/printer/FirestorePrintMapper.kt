package com.it10x.foodappgstav7_06.printer

import com.it10x.foodappgstav7_06.data.online.models.OrderMasterData
import com.it10x.foodappgstav7_06.data.online.models.OrderProductData
import com.it10x.foodappgstav7_06.data.online.models.formattedTime

object FirestorePrintMapper {

    fun map(
        order: OrderMasterData,
        items: List<OrderProductData>
    ): PrintOrder {

        val printItems = items.map { item ->
            PrintItem(
                name = item.name,
                quantity = item.quantity,
                price = toDouble(item.price),
                subtotal = toDouble(item.itemSubtotal),
                note = item.note ?: "",
                modifiersJson = item.modifiersJson ?: ""
            )
        }

        return PrintOrder(
            // ---------- CORE ----------
            orderNo = order.srno.toString(),
            customerName = order.customerName.ifBlank { "Walk-in" },
            dateTime = order.formattedTime(),

            // ---------- ORDER TYPE ----------
            orderType = order.orderType,
            tableNo = order.tableNo,

            // ---------- DELIVERY SNAPSHOT (d*) ----------
            dAddressLine1 = order.dAddressLine1,
            dAddressLine2 = order.dAddressLine2,
            dCity = order.dCity,
            dState = order.dState,
            dZipcode = order.dZipcode,
            customerPhone = order.customerPhone,
            dLandmark = order.dLandmark,

            // ---------- ITEMS ----------
            items = printItems,

            // ---------- TOTALS ----------
            itemTotal = toDouble(order.itemTotal),
            deliveryFee = toDouble(order.deliveryFee),
            discount = toDouble(order.discountTotal),
            tax = toDouble(order.taxTotal),
            grandTotal = toDouble(order.grandTotal)
        )
    }

    private fun toDouble(value: Any?): Double =
        when (value) {
            is Double -> value
            is Long -> value.toDouble()
            is Int -> value.toDouble()
            is Float -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: 0.0
            else -> 0.0
        }


}
