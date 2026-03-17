package com.it10x.foodappgstav7_06.data.online.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * OrderMasterData (PRODUCTION READY)
 *
 * Used for:
 * POS
 * Web Orders
 * Printing
 * Firestore
 * Analytics
 */
data class OrderMasterData(

    // =====================================================
    // CORE IDENTIFIERS
    // =====================================================
    var id: String = "",
    var srno: Int = 0,
    var source: String? = null, // POS / WEB / APP
    var orderNumber: String = "", // Human readable order number (#1023)


    // =====================================================
    // CUSTOMER
    // =====================================================
    var customerId: String? = null,
    var customerName: String = "",
    var email: String = "",
    var addressId: String = "",
    var customerPhone: String? = null,


    // =====================================================
    // CUSTOMER ADDRESS (PRINT SNAPSHOT)
    // =====================================================
    var dAddressLine1: String? = null,
    var dAddressLine2: String? = null,
    var dCity: String? = null,
    var dState: String? = null,
    var dZipcode: String? = null,
    var dLandmark: String? = null,


    // =====================================================
    // ORDER TYPE
    // =====================================================
    var orderType: String? = null, // DELIVERY / PICKUP / DINE_IN
    var tableNo: String? = null,


    // =====================================================
    // AMOUNTS
    // =====================================================
    var itemTotal: Double = 0.0,
    var subTotal: Double? = null,
    var discountTotal: Double? = null,
    var taxTotal: Double? = null,
    var deliveryFee: Double? = null,
    var grandTotal: Double? = null,


    // =====================================================
    // PAYMENT
    // =====================================================
    var paymentType: String = "", // CASH / CARD / ONLINE
    var paymentStatus: String? = null,


    // =====================================================
    // ORDER FLOW
    // =====================================================
    var orderStatus: String? = null, // PENDING / COMPLETED / CANCELLED


    // =====================================================
    // OUTLET (MULTI-LOCATION)
    // =====================================================
    var outletId: String? = null,
    var outletName: String? = null,


    // =====================================================
    // POS HELPERS
    // =====================================================
    var productsCount: Int? = null,
    var notes: String? = null,


    // =====================================================
    // TIMESTAMPS (IMPORTANT)
    // =====================================================
    var createdAt: Timestamp? = null,
    var createdAtMillis: Long = 0L,

    // Fast search fields
    var orderDate: String = "",   // "2026-03-13"
    var orderMonth: String = "",  // "2026-03"
    var orderYear: Int = 0,


    // =====================================================
    // AUTOMATION
    // =====================================================
    var printed: Boolean? = null,
    var acknowledged: Boolean? = null,


    // =====================================================
    // SYNC CONTROL
    // =====================================================
    var syncStatus: String? = null,


    // =====================================================
    // LEGACY / DISCOUNTS
    // =====================================================
    var couponFlat: Double? = null,
    var pickUpDiscount: Double? = null,
    var couponPercent: Double? = null,
)


// =====================================================
// EXTENSIONS
// =====================================================

// Format time for printing
fun OrderMasterData.formattedTime(): String {

    val millis = createdAt?.toDate()?.time ?: createdAtMillis

    if (millis == 0L) return "--"

    val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

    return sdf.format(Date(millis))
}


// Build delivery address for printing
fun OrderMasterData.fullDeliveryAddress(): String? {

    val parts = listOfNotNull(
        dAddressLine1,
        dAddressLine2,
        dLandmark,
        dCity,
        dState,
        dZipcode
    ).filter { it.isNotBlank() }

    return if (parts.isEmpty()) null else parts.joinToString(", ")
}