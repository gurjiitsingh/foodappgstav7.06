package com.it10x.foodappgstav7_06.printer

import android.util.Log
import com.it10x.foodappgstav7_06.data.pos.entities.PosKotItemEntity
import com.it10x.foodappgstav7_06.data.print.OutletInfo
import com.it10x.foodappgstav7_06.ui.sales.SalesUiState

// -----------------------------
// PRINT MODELS (ONE TRUTH)
// -----------------------------


// -----------------------------
// RECEIPT FORMATTER
// -----------------------------

object ReceiptFormatter {

    private const val LINE_WIDTH = 32
    private const val ALIGN_LEFT = "\u001B\u0061\u0000"





    // -----------------------------
    // BILLING RECEIPT
    // -----------------------------
    fun billing(order: PrintOrder, outletInfo: OutletInfo): String {

        val LINE_WIDTH = 32
        Log.d("RECEIPT_FORMATTER", "billing48() called for orderNo=${order.orderNo}")
        val outletHeader = buildOutletHeader(outletInfo, LINE_WIDTH)

        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(16) +
                        "PRICE".padStart(6) +
                        "TOTAL".padStart(6)

            val divider = "-".repeat(LINE_WIDTH)

//            val lines = order.items.joinToString("\n") { item ->
//                val qty = item.quantity.toString().padEnd(4)
//                val name = item.name.take(16).padEnd(16)
//                val price = format(item.price).padStart(6)
//                val total = format(item.subtotal).padStart(6)
//                qty + name + price + total
//            }

            val lines = buildString {

                order.items.forEach { item ->

                    val qty = item.quantity.toString().padEnd(4)
                    val name = item.name.take(16).padEnd(16)
                    val price = format(item.price).padStart(6)
                    val total = format(item.subtotal).padStart(6)

                    // 🔹 Main item line (32 chars total)
                    append(qty + name + price + total + "\n")

                    // 🔹 Modifiers (indented, max width safe)
                    if (!item.modifiersJson.isNullOrBlank()) {
                        try {
                            val modifiers = item.modifiersJson
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",")
                                .map { it.trim().replace("\"", "") }
                                .filter { it.isNotBlank() }

                            modifiers.forEach { mod ->
                                append("    + ${mod.take(26)}\n")
                            }
                        } catch (_: Exception) {
                            append("    + ${item.modifiersJson.take(26)}\n")
                        }
                    }

                    // 🔹 Note (indented)
                    if (!item.note.isNullOrBlank()) {
                        append("    • ${item.note.take(26)}\n")
                    }
                }
            }


            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """

------------------------------
$outletHeader
------------------------------
$headerBlock
----------------------------
$itemsBlock
------------------------------
${totalLine("Item Total", order.itemTotal)}
${totalLine("Delivery", order.deliveryFee)}
${totalLine("Discount", order.discount)}
${totalLine("Tax", order.tax)}
------------------------------
${totalLine("GRAND TOTAL", order.grandTotal)}
------------------------------
${buildOutletFooter(outletInfo, 32)}
Thank You!


""".trimIndent()
            )
        }
    }


    fun billing48(order: PrintOrder, outletInfo: OutletInfo): String {

        val LINE_WIDTH = 48
      //  Log.d("RECEIPT_FORMATTER", "billing48() called for orderNo=${order.orderNo}")
        val outletHeader = buildOutletHeader(outletInfo, LINE_WIDTH)




        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            // 48 chars total → distribute: qty(4) + name(26) + price(8) + total(10)
            val header =
                "QTY".padEnd(4) +
                        "ITEM".padEnd(26) +
                        "PRICE".padStart(8) +
                        "TOTAL".padStart(10)

            val divider = "-".repeat(LINE_WIDTH)

//            val lines = order.items.joinToString("\n") { item ->
//                val qty = item.quantity.toString().padEnd(4)
//                val name = item.name.take(26).padEnd(26)
//                val price = format(item.price).padStart(8)
//                val total = format(item.subtotal).padStart(10)
//                qty + name + price + total
//            }

            val lines = buildString {

                order.items.forEach { item ->

                    val qty = item.quantity.toString().padEnd(4)
                    val name = item.name.take(26).padEnd(26)
                    val price = format(item.price).padStart(8)
                    val total = format(item.subtotal).padStart(10)

                    // 🔹 Main line
                    append(qty + name + price + total + "\n")

                    // 🔹 Modifiers (if any)
                    if (!item.modifiersJson.isNullOrBlank()) {
                        try {
                            val modifiers = item.modifiersJson
                                .removePrefix("[")
                                .removeSuffix("]")
                                .split(",")
                                .map { it.trim().replace("\"", "") }
                                .filter { it.isNotBlank() }

                            modifiers.forEach { mod ->
                                append("    + $mod\n")
                            }
                        } catch (_: Exception) {
                            append("    + ${item.modifiersJson}\n")
                        }
                    }

                    // 🔹 Note (if any)
                    if (!item.note.isNullOrBlank()) {
                        append("    • ${item.note}\n")
                    }
                }
            }


            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
------------------------------------------------
$outletHeader
------------------------------------------------
$headerBlock
------------------------------------------------
$itemsBlock
------------------------------------------------
${totalLine48("Item Total", order.itemTotal)}
${totalLine48("Delivery", order.deliveryFee)}
${totalLine48("Discount", order.discount)}
${totalLine48("Tax", order.tax)}
------------------------------------------------
${totalLine48("GRAND TOTAL", order.grandTotal)}
------------------------------------------------
${buildOutletFooter(outletInfo, 48)}
Thank You!


""".trimIndent()
            )
        }
    }


    // -----------------------------
    // KITCHEN RECEIPT
    // -----------------------------

    fun kitchen(items: List<PosKotItemEntity>, title: String = "KITCHEN"): String {

        val itemsBlock = if (items.isEmpty()) {
            "No items"
        } else {
            items.joinToString("\n") {
                "${it.quantity.toString().padEnd(3)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********

------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }
    fun kitchen1(order: PrintOrder, title: String = "KITCHEN"): String {

        val itemsBlock = if (order.items.isEmpty()) {
            "No items"
        } else {
            order.items.joinToString("\n") {
                "${it.quantity.toString().padEnd(3)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********
Order No : ${order.orderNo}
------------------------
$itemsBlock
------------------------


""".trimIndent()
            )
        }
    }



    fun kitchen83(order: PrintOrder, title: String = "KITCHEN"): String {
        val itemsBlock = if (order.items.isEmpty()) {
            "No items"
        } else {
            order.items.joinToString("\n") {
                "${it.quantity.toString().padEnd(4)} ${it.name}"
            }
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
******** $title ********
Order No : ${order.orderNo}
${"-".repeat(48)}
$itemsBlock
${"-".repeat(48)}


""".trimIndent()
            )
        }
    }


    // -----------------------------
    // HEADER LOGIC (IMPORTANT)
    // -----------------------------
    private fun buildHeaderBlock(order: PrintOrder): String {

        val base = mutableListOf<String>()

        // --- Common fields ---
        base.add("Order No : ${order.orderNo}")
        base.add("Customer : ${order.customerName.ifBlank { "Walk-in" }}")
        base.add("Date     : ${order.dateTime}")

        when (order.orderType) {

            // -----------------------------
            // DINE-IN ORDERS
            // -----------------------------
            "DINE_IN" -> {
                order.tableNo?.takeIf { it.isNotBlank() }?.let {
                    base.add("Table    : $it")
                }
            }

            // -----------------------------
            // TAKEAWAY ORDERS
            // -----------------------------
            "TAKEAWAY" -> {
                // Only show phone if filled
                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }

            // -----------------------------
            // DELIVERY / ONLINE ORDERS
            // -----------------------------
            "DELIVERY", "ONLINE" -> {
                val addressLines = mutableListOf<String>()

                order.dAddressLine1?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dAddressLine2?.takeIf { it.isNotBlank() }?.let { addressLines.add(it) }
                order.dLandmark?.takeIf { it.isNotBlank() }?.let { addressLines.add("Landmark: $it") }

                // City + Zip
                listOfNotNull(order.dCity, order.dZipcode)
                    .joinToString(" ")
                    .takeIf { it.isNotBlank() }
                    ?.let { addressLines.add(it) }

                if (addressLines.isNotEmpty()) {
                    base.add("Address  :")
                    base.addAll(addressLines)
                }

                order.customerPhone?.takeIf { it.isNotBlank() }?.let {
                    base.add("Phone    : $it")
                }
            }
        }

        return base.joinToString("\n")
    }



    // -----------------------------
    // HELPERS
    // -----------------------------
    private fun totalLine(label: String, value: Double): String {
        if (value == 0.0) return ""
        val left = label.padEnd(14)
        val right = format(value).padStart(18)
        return left + right
    }

    private fun format(value: Double): String = "%.2f".format(value)


    fun posKitchen(
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        title: String = "KITCHEN"
    ): String {

        val time = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val header = buildString {
            append("******** $title ********\n")
            append("Type  : $orderType\n")
            append("Ref   : $sessionKey\n")
            append("Time  : $time\n")
            append("------------------------\n")
        }



        val itemsBlock =
            if (items.isEmpty()) {
                "No items\n"
            } else {
                buildString {
                    items.forEach { item ->

                        // 🔹 Main item line
                        append("${item.quantity.toString().padEnd(3)} ${item.name}\n")

                        // 🔹 Modifiers (if any)
                        if (item.modifiersJson.isNotEmpty()) {
                            try {
                                val modifiers = item.modifiersJson
                                    .removePrefix("[")
                                    .removeSuffix("]")
                                    .split(",")
                                    .map { it.trim().replace("\"", "") }
                                    .filter { it.isNotBlank() }

                                modifiers.forEach { modifier ->
                                    append("      + $modifier\n")
                                }
                            } catch (_: Exception) {
                                append("      + ${item.modifiersJson}\n")
                            }
                        }

                        // 🔹 Note (if any)
                        if (item.note.isNotEmpty()) {
                            append("   ${item.note}\n")
                        }

                        append("\n")
                    }
                }
            }

//        val modifiers = Gson().fromJson(
//            item.modifiersJson,
//            Array<String>::class.java
//        )
//        Instead of manual split.
//        But current version works fine.


        return buildString {
            append(ALIGN_LEFT)
            append(header)
            append(itemsBlock)
            append("------------------------\n\n")
        }
    }



    fun posKitchenFontVar(
        sessionKey: String,
        orderType: String,
        items: List<PosKotItemEntity>,
        title: String = "KITCHEN"
    ): String {

        val TEXT_NORMAL = "\u001D!\u0000"
        val TEXT_DOUBLE_HEIGHT = "\u001B!\u0010"
        val TEXT_DOUBLE_WIDTH = "\u001B!\u0001"
        val TEXT_BIG = "\u001D!\u0011"

        val time = java.text.SimpleDateFormat(
            "HH:mm",
            java.util.Locale.getDefault()
        ).format(java.util.Date())

        val header = buildString {
            append(TEXT_BIG)
            append("******** $title ********\n")
            append(TEXT_NORMAL)
            append("Type  : $orderType\n")
            append("Ref   : $sessionKey\n")
            append("Time  : $time\n")
            append("------------------------\n")
        }



        val itemsBlock =
            if (items.isEmpty()) {
                "No items\n"
            } else {
                buildString {
                    items.forEach { item ->

                        // 🔹 Main item line
                        append("${item.quantity.toString().padEnd(3)} ${item.name}\n")

                        // 🔹 Modifiers (if any)
                        if (item.modifiersJson.isNotEmpty()) {
                            try {
                                val modifiers = item.modifiersJson
                                    .removePrefix("[")
                                    .removeSuffix("]")
                                    .split(",")
                                    .map { it.trim().replace("\"", "") }
                                    .filter { it.isNotBlank() }

                                modifiers.forEach { modifier ->
                                    append("      + $modifier\n")
                                }
                            } catch (_: Exception) {
                                append("      + ${item.modifiersJson}\n")
                            }
                        }

                        // 🔹 Note (if any)
                        if (item.note.isNotEmpty()) {
                            append("     ${item.note}\n")
                        }

                        append("\n")
                    }
                }
            }

//        val modifiers = Gson().fromJson(
//            item.modifiersJson,
//            Array<String>::class.java
//        )
//        Instead of manual split.
//        But current version works fine.


        return buildString {
            append(ALIGN_LEFT)
            append(header)
            append(itemsBlock)
            append("------------------------\n\n")
        }
    }


    // -----------------------------
// INTERNAL HELPERS (dynamic lineWidth)
// -----------------------------
    private fun billingWithWidth(order: PrintOrder, title: String, lineWidth: Int): String {

        val headerBlock = buildHeaderBlock(order)

        val itemsBlock = if (order.items.isEmpty()) {
            "No items found"
        } else {
            val header =
                "QTY".padEnd(5) +               // slightly more space for qty
                        "ITEM".padEnd(lineWidth - 20) + // dynamic item width
                        "PRICE".padStart(7) +
                        "TOTAL".padStart(7)

            val divider = "-".repeat(lineWidth)

            val lines = order.items.joinToString("\n") { item ->
                val qty = item.quantity.toString().padEnd(5)
                val name = item.name.take(lineWidth - 20).padEnd(lineWidth - 20)
                val price = format(item.price).padStart(7)
                val total = format(item.subtotal).padStart(7)
                qty + name + price + total
            }

            "$header\n$divider\n$lines"
        }

        return buildString {
            append(ALIGN_LEFT)
            append(
                """
${"-".repeat(lineWidth)}
$title
${"-".repeat(lineWidth)}
$headerBlock
${"-".repeat(lineWidth)}
$itemsBlock
${"-".repeat(lineWidth)}
${totalLineWidth("Item Total", order.itemTotal, lineWidth)}
${totalLineWidth("Delivery", order.deliveryFee, lineWidth)}
${totalLineWidth("Discount", order.discount, lineWidth)}
${totalLineWidth("Tax", order.tax, lineWidth)}
${"-".repeat(lineWidth)}
${totalLineWidth("GRAND TOTAL", order.grandTotal, lineWidth)}
${"-".repeat(lineWidth)}
Thank You!


""".trimIndent()
            )
        }
    }

    // totalLine helper for dynamic width
    private fun totalLineWidth(label: String, value: Double, lineWidth: Int): String {
        if (value == 0.0) return ""
        val left = label.padEnd(lineWidth - 12)
        val right = format(value).padStart(12)
        return left + right
    }


    private fun totalLine48(label: String, amount: Double): String {
        val formatted = format(amount)
        // label left-aligned, amount right-aligned to total 48 characters
        val space = 48 - label.length - formatted.length
        return label + " ".repeat(if (space > 0) space else 1) + formatted
    }


    private fun buildOutletHeader(info: OutletInfo, width: Int): String {
        val lines = mutableListOf<String>()
        if (info.name.isNotBlank()) lines += centerText(info.name, width)
        info.addressLine1
            ?.takeIf { it.isNotBlank() }
            ?.let { address ->
                lines += address.take(width)
            }
       if(width==32){
            info.addressLine2?.let { lines += it.take(width) }
            info.addressLine3?.let { lines += it.take(width) }
            info.city?.let { lines += it.take(width) }
            info.phone?.let { lines += "Phone: $it" }
            info.phone2?.let { lines += " $it" }
            info.email?.let { lines += "Email: $it" }
            info.web?.let { lines += "Web: $it" }
            info.gst?.let { lines += "GST: $it" }
         //   info.footerNote?.let { lines += it.take(width) }
        }

        if(width==48){
            info.addressLine2?.let { lines += it.take(width) }
            info.addressLine3?.let { lines += it.take(width) }
            info.city?.let { lines += it.take(width) }
            val phone1 = info.phone?.takeIf { it.isNotBlank() }
            val phone2 = info.phone2?.takeIf { it.isNotBlank() }

            if (phone1 != null && phone2 != null) {
                // Both phones available
                lines += "Phone: $phone1, $phone2".take(width)
            } else if (phone1 != null) {
                // Only first phone
                lines += "Phone: $phone1".take(width)
            } else if (phone2 != null) {
                // Only second phone
                lines += "Phone: $phone2".take(width)
            }
            info.email?.let { lines += "Email: $it" }
            info.web?.let { lines += "Web: $it" }
            info.gst?.let { lines += "GST: $it" }
           // info.footerNote?.let { lines += it.take(width) }
        }

        return lines.joinToString("\n")
    }

    private fun centerText(text: String, width: Int): String {
        val pad = (width - text.length) / 2
        return " ".repeat(maxOf(pad, 0)) + text
    }


    private fun buildOutletFooter(info: OutletInfo, width: Int): String {

        val note = info.footerNote?.trim()

        // 🔹 If null or blank → return empty (do not show anything)
        if (note.isNullOrBlank()) return ""

        val wrappedLines = wrapText(note, width)

        return buildString {
            append("-".repeat(width))
            append("\n")

            wrappedLines.forEach { line ->
                append(line)     // left aligned (better for readability)
                append("\n")
            }

            append("-".repeat(width))
            append("\n")
        }
    }

    private fun wrapText(text: String, width: Int): List<String> {
        val words = text.split("\\s+".toRegex())
        val lines = mutableListOf<String>()
        var currentLine = ""

        for (word in words) {

            // If single word itself is longer than width
            if (word.length >= width) {
                if (currentLine.isNotBlank()) {
                    lines.add(currentLine.trim())
                    currentLine = ""
                }

                // break long word safely
                word.chunked(width).forEach {
                    lines.add(it)
                }

                continue
            }

            if ((currentLine + word).length + 1 > width) {
                lines.add(currentLine.trim())
                currentLine = "$word "
            } else {
                currentLine += "$word "
            }
        }

        if (currentLine.isNotBlank()) {
            lines.add(currentLine.trim())
        }

        return lines
    }

    fun salesReport(
        state: SalesUiState,
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val divider = "-".repeat(width)

        fun line(label: String, value: Double): String {
            val formatted = "%.2f".format(value)
            val space = width - label.length - formatted.length
            return label + " ".repeat(if (space > 0) space else 1) + formatted
        }

        val header = buildOutletHeader(outletInfo, width)

        return buildString {
            append("\u001B\u0061\u0000") // align left
            append(header + "\n")
            append(divider + "\n")
            append("SALES REPORT\n")
            append(divider + "\n")

            append("From : ${java.util.Date(state.from)}\n")
            append("To   : ${java.util.Date(state.to)}\n")
            append(divider + "\n")

            append(line("Total Sales", state.totalSales) + "\n")
            append(line("Tax", state.taxTotal) + "\n")
            append(line("Discount", state.discountTotal) + "\n")

            append(divider + "\n")

            state.paymentBreakup.forEach { (type, amount) ->
                append(line(type, amount) + "\n")
            }

            append(divider + "\n")
            append(line("Food", state.foodTotal) + "\n")
            append(line("Beverages", state.beveragesTotal) + "\n")
            append(line("Wine", state.wineTotal) + "\n")

            append(divider + "\n\n")
        }
    }


    fun salesCategorySummary(
        category: String,
        totalQty: Int,
        totalAmount: Double,
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val divider = "-".repeat(width)

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }

        val header = buildOutletHeader(outletInfo, width)

        return buildString {

            append("\u001B\u0061\u0000")
            append(header + "\n")
            append(divider + "\n")

            append("CATEGORY SUMMARY\n")
            append(category.uppercase() + "\n")
            append(divider + "\n")

            append(line("Total Qty", totalQty.toString()) + "\n")
            append(line("Total Amount", "₹ %.2f".format(totalAmount)) + "\n")

            append(divider + "\n\n")
        }
    }


    fun salesBySingleCategory(
        category: String,
        items: Map<String, Pair<Int, Double>>,
        outletInfo: OutletInfo,
        width: Int
    ): String {

        val divider = "-".repeat(width)

        fun line(label: String, value: String): String {
            val safeLabel = label.take(width - value.length - 1)
            val space = width - safeLabel.length - value.length
            return safeLabel + " ".repeat(if (space > 0) space else 1) + value
        }

        val header = buildOutletHeader(outletInfo, width)

        return buildString {

            append("\u001B\u0061\u0000")
            append(header + "\n")
            append(divider + "\n")
            append("CATEGORY REPORT\n")
            append(category.uppercase() + "\n")
            append(divider + "\n")

            if (items.isEmpty()) {
                append("No sales data\n")
            } else {

                items.forEach { (itemName, data) ->

                    val qty = data.first
                    val total = data.second
                    val rate = if (qty > 0) total / qty else 0.0

                    append(itemName.take(width) + "\n")

                    val qtyRate = "$qty x %.2f".format(rate)
                    val totalStr = "%.2f".format(total)

                    append(line(qtyRate, totalStr) + "\n")
                    append("\n")
                }
            }

            append(divider + "\n\n")
        }
    }



}
