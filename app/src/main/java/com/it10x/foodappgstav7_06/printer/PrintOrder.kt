package com.it10x.foodappgstav7_06.printer

/**
 * Master print model
 * Used by BOTH:
 *  - POS Local Orders
 *  - Firestore Online Orders
 *
 * ⚠️ DELIVERY FIELDS USE d* EVERYWHERE (STANDARD)
 */
data class PrintOrder(

    // ---------- CORE ----------
    val orderNo: String,
    val customerName: String,
    val dateTime: String,

    // ---------- ORDER TYPE ----------
    val orderType: String? = null,   // DINE_IN | TAKEAWAY | DELIVERY | ONLINE
    val tableNo: String? = null,

    // ---------- DELIVERY SNAPSHOT (d*) ----------
    val customerPhone: String? = null,
    val dAddressLine1: String? = null,
    val dAddressLine2: String? = null,
    val dCity: String? = null,
    val dState: String? = null,
    val dZipcode: String? = null,
    val dLandmark: String? = null,

    // ---------- ITEMS ----------
    val items: List<PrintItem>,

    // ---------- TOTALS ----------
    val itemTotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val discount: Double = 0.0,
    val grandTotal: Double
)

/**
 * Line item printed on receipt
 */
data class PrintItem(
    val name: String,
    val quantity: Int,
    val price: Double = 0.0,
    val subtotal: Double = 0.0,
    val note: String?,
    val modifiersJson: String?,

)
